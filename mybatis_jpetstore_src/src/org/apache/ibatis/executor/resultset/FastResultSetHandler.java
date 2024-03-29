package org.apache.ibatis.executor.resultset;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.loader.ResultObjectProxy;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.*;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.*;
import java.util.*;

/**
 * SQL执行结果的处理工具 
 */
public class FastResultSetHandler implements ResultSetHandler {

  protected final Executor executor;
  protected final Configuration configuration;
  protected final MappedStatement mappedStatement;
  protected final RowBounds rowBounds;
  protected final ParameterHandler parameterHandler;
  protected final ResultHandler resultHandler;
  protected final BoundSql boundSql;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ObjectFactory objectFactory;

  public FastResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    this.executor = executor;
    this.configuration = mappedStatement.getConfiguration();
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;
    this.parameterHandler = parameterHandler;
    this.boundSql = boundSql;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.resultHandler = resultHandler;
  }

  //
  // HANDLE OUTPUT PARAMETER
  //

  public void handleOutputParameters(CallableStatement cs) throws SQLException {
    final Object parameterObject = parameterHandler.getParameterObject();
    final MetaObject metaParam = configuration.newMetaObject(parameterObject);
    final List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    for (int i = 0; i < parameterMappings.size(); i++) {
      final ParameterMapping parameterMapping = parameterMappings.get(i);
      if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
        if ("java.sql.ResultSet".equalsIgnoreCase(parameterMapping.getJavaType().getName())) {
          handleRefCursorOutputParameter(cs, parameterMapping, i, metaParam);
        } else {
          final TypeHandler typeHandler = parameterMapping.getTypeHandler();
          if (typeHandler == null) {
            throw new ExecutorException("Type handler was null on parameter mapping for property " + parameterMapping.getProperty() + ".  " +
                "It was either not specified and/or could not be found for the javaType / jdbcType combination specified.");
          }
          metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1));
        }
      }
    }
  }

  protected void handleRefCursorOutputParameter(CallableStatement cs, ParameterMapping parameterMapping, int parameterMappingIndex, MetaObject metaParam) throws SQLException {
    final ResultSet rs = (ResultSet) cs.getObject(parameterMappingIndex + 1);
    final String resultMapId = parameterMapping.getResultMapId();
    if (resultMapId != null) {
      final ResultMap resultMap = configuration.getResultMap(resultMapId);
      final DefaultResultHandler resultHandler = new DefaultResultHandler();
      handleRowValues(rs, resultMap, resultHandler, new RowBounds());
      metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList());
    } else {
      throw new ExecutorException("Parameter requires ResultMap for output types of java.sql.ResultSet");
    }
    rs.close();
  }

  //
  // HANDLE RESULT SETS
  //

  public List handleResultSets(Statement stmt) throws SQLException {
    final List multipleResults = new ArrayList();
    final List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    int resultSetCount = 0;
    ResultSet rs = stmt.getResultSet();

    while (rs == null) {
        // move forward to get the first resultset in case the driver
        // doesn't return the resultset as the first result (HSQLDB 2.1)
    	// 看注释，貌似是对HSQLDB 2.1的特殊处理
        if (stmt.getMoreResults()) {
            rs = stmt.getResultSet();
        } else {
            if (stmt.getUpdateCount() == -1) {
                // no more results.  Must be no resultset
                break;
            }
        }
    }
    
    validateResultMapsCount(rs, resultMapCount);
    //循环 处理resultSet(因为可能有多个，所以resultMap属性在xml文件中可以配置多个，以","号隔开)
    while (rs != null && resultMapCount > resultSetCount) {
      final ResultMap resultMap = resultMaps.get(resultSetCount);
      handleResultSet(rs, resultMap, multipleResults);
      //循环下一个(如果有)
      rs = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }
    return collapseSingleResultList(multipleResults);
  }

  protected void closeResultSet(ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      // ignore
    }
  }
  
  protected void cleanUpAfterHandlingResultSet() {
  }

  protected void validateResultMapsCount(ResultSet rs, int resultMapCount) {
    if (rs != null && resultMapCount < 1) {
      throw new ExecutorException(
          "A query was run and no Result Maps were found for the Mapped Statement '"
              + mappedStatement.getId()
              + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
    }
  }
  /**
   * 处理 "数据库操作结果"
   */
  @SuppressWarnings("unchecked")
protected void handleResultSet(ResultSet rs, ResultMap resultMap, List multipleResults) throws SQLException {
    try {
      if (resultHandler == null) {
        DefaultResultHandler defaultResultHandler = new DefaultResultHandler();
        //循环处理ResultSet
        handleRowValues(rs, resultMap, defaultResultHandler, rowBounds);
        multipleResults.add(defaultResultHandler.getResultList());
      } else {
        handleRowValues(rs, resultMap, resultHandler, rowBounds);
      }
    } finally {
      //用完关闭	
      closeResultSet(rs); // issue #228 (close resultsets)
    }
  }

  protected List collapseSingleResultList(List multipleResults) {
    if (multipleResults.size() == 1) {
      return (List) multipleResults.get(0);
    } else {
      return multipleResults;
    }
  }

  
/**
 * 处理 "数据库操作结果",包含基于内存的分页处理
 */
  protected void handleRowValues(ResultSet rs, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds) throws SQLException {
    final DefaultResultContext resultContext = new DefaultResultContext();
    //起始位置定位
    skipRows(rs, rowBounds);
    //循环处理结果
    while (shouldProcessMoreRows(rs, resultContext, rowBounds)) {
      //处理resultMap配置中的discriminator
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rs, resultMap);
      Object rowValue = getRowValue(rs, discriminatedResultMap, null);
      //计数
      resultContext.nextResultObject(rowValue);
      resultHandler.handleResult(resultContext);
    }
  }

  protected boolean shouldProcessMoreRows(ResultSet rs, ResultContext context, RowBounds rowBounds) throws SQLException {
    return rs.next() && context.getResultCount() < rowBounds.getLimit() && !context.isStopped();
  }

  /**
   *  因为mybatis的分页是基于内存的分页(先不说这种方式不好)，
   *  所以这里要直接把resultSet定位到rowBounds指定的起始位置
   */
  protected void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
	//数据库是否支持ResultSet#absolute
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
      if (rowBounds.getOffset() != 0) {
        rs.absolute(rowBounds.getOffset());
      }
    } else {
      //只能一个一个next啦
      for (int i = 0; i < rowBounds.getOffset(); i++) rs.next();
    }
  }

  /**
   * 判断是否有多个resultSet，如果有，返回下一个resultSet
   */
  protected ResultSet getNextResultSet(Statement stmt) throws SQLException {
	//tolerant( [美][ˈtɑlərənt]  宽容的；容忍的，忍受的)
    // Making this method tolerant of bad JDBC drivers
    try {
      if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
        // Crazy Standard JDBC way of determining if there are more results
        if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
          return stmt.getResultSet();
        }
      }
    } catch (Exception e) {
      // Intentionally ignored.
    }
    return null;
  }

  //
  // GET VALUE FROM ROW
  //

  protected Object getRowValue(ResultSet rs, ResultMap resultMap, CacheKey rowKey) throws SQLException {
    final List<String> mappedColumnNames = new ArrayList<String>();
    final List<String> unmappedColumnNames = new ArrayList<String>();
    //
    final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
    Object resultObject = createResultObject(rs, resultMap, lazyLoader);
    if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      loadMappedAndUnmappedColumnNames(rs, resultMap, mappedColumnNames, unmappedColumnNames);
      boolean foundValues = resultMap.getConstructorResultMappings().size() > 0;
      if (!AutoMappingBehavior.NONE.equals(configuration.getAutoMappingBehavior())) {
        foundValues = applyAutomaticMappings(rs, unmappedColumnNames, metaObject) || foundValues;
      }
      foundValues = applyPropertyMappings(rs, resultMap, mappedColumnNames, metaObject, lazyLoader) || foundValues;
      foundValues = (lazyLoader != null && lazyLoader.size() > 0) || foundValues;
      resultObject = foundValues ? resultObject : null;
      return resultObject;
    }
    return resultObject;
  }

  protected ResultLoaderMap instantiateResultLoaderMap() {
    if (configuration.isLazyLoadingEnabled()) {
      return new ResultLoaderMap();
    } else {
      return null;
    }
  }

  //
  // PROPERTY MAPPINGS
  //

  protected boolean applyPropertyMappings(ResultSet rs, ResultMap resultMap, List<String> mappedColumnNames, MetaObject metaObject, ResultLoaderMap lazyLoader) throws SQLException {
    boolean foundValues = false;
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      final String column = propertyMapping.getColumn();
      if (propertyMapping.isCompositeResult() || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH)))) {
        Object value = getPropertyMappingValue(rs, metaObject, propertyMapping, lazyLoader);
        if (value != null) {
          final String property = propertyMapping.getProperty();
          metaObject.setValue(property, value);
          foundValues = true;
        }
      }
    }
    return foundValues;
  }

  protected Object getPropertyMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader) throws SQLException {
    final TypeHandler typeHandler = propertyMapping.getTypeHandler();
    if (propertyMapping.getNestedQueryId() != null) {
      return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader);
    } else if (typeHandler != null) {
      final String column = propertyMapping.getColumn();
      return typeHandler.getResult(rs, column);
    }
    return null;
  }

  protected boolean applyAutomaticMappings(ResultSet rs, List<String> unmappedColumnNames, MetaObject metaObject) throws SQLException {
    boolean foundValues = false;
    for (String columnName : unmappedColumnNames) {
      final String property = metaObject.findProperty(columnName);
      if (property != null) {
        final Class propertyType = metaObject.getSetterType(property);
        if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
          final TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(propertyType);
          final Object value = typeHandler.getResult(rs, columnName);
          if (value != null) {
            metaObject.setValue(property, value);
            foundValues = true;
          }
        }
      }
    }
    return foundValues;
  }

  protected void loadMappedAndUnmappedColumnNames(ResultSet rs, ResultMap resultMap, List<String> mappedColumnNames, List<String> unmappedColumnNames) throws SQLException {
    mappedColumnNames.clear();
    unmappedColumnNames.clear();
    final ResultSetMetaData rsmd = rs.getMetaData();
    final int columnCount = rsmd.getColumnCount();
    final Set<String> mappedColumns = resultMap.getMappedColumns();
    for (int i = 1; i <= columnCount; i++) {
      //getColumnLabel是sql语句中as ***的名称，getColumnName是字段名
      final String columnName = configuration.isUseColumnLabel() ? rsmd.getColumnLabel(i) : rsmd.getColumnName(i);
      final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
      if (mappedColumns.contains(upperColumnName)) {
        mappedColumnNames.add(upperColumnName);
        mappedColumnNames.add(columnName);
      } else {
        unmappedColumnNames.add(upperColumnName);
        unmappedColumnNames.add(columnName);
      }
    }
  }

  //
  // INSTANTIATION & CONSTRUCTOR MAPPING
  //

  protected Object createResultObject(ResultSet rs, ResultMap resultMap, ResultLoaderMap lazyLoader) throws SQLException {
    final List<Class> constructorArgTypes = new ArrayList<Class>();
    final List<Object> constructorArgs = new ArrayList<Object>();
    final Object resultObject = createResultObject(rs, resultMap, constructorArgTypes, constructorArgs);
    if (resultObject != null && configuration.isLazyLoadingEnabled()) {
      return ResultObjectProxy.createProxy(resultObject, lazyLoader, configuration.isAggressiveLazyLoading(), objectFactory, constructorArgTypes, constructorArgs);
    }
    return resultObject;
  }

  protected Object createResultObject(ResultSet rs, ResultMap resultMap, List<Class> constructorArgTypes, List<Object> constructorArgs)
      throws SQLException {
    final Class resultType = resultMap.getType();
    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    if (typeHandlerRegistry.hasTypeHandler(resultType)) {
      //typeHandlerRegistry中已经注册的类型(默认的mybatis能处理java基本数据类型、java.sql.*定义的一些类型、{@link org.apache.ibatis.type.JdbcType}定义的一些类型)
      return createPrimitiveResultObject(rs, resultMap);
    } else if (constructorMappings.size() > 0) {
      return createParameterizedResultObject(rs, resultType, constructorMappings, constructorArgTypes, constructorArgs);
    } else {
      //
      return objectFactory.create(resultType);
    }
  }

  protected Object createParameterizedResultObject(ResultSet rs, Class resultType,
      List<ResultMapping> constructorMappings, List<Class> constructorArgTypes, List<Object> constructorArgs) throws SQLException {
    boolean foundValues = false;
    for (ResultMapping constructorMapping : constructorMappings) {
      final Class parameterType = constructorMapping.getJavaType();
      final TypeHandler typeHandler = constructorMapping.getTypeHandler();
      final String column = constructorMapping.getColumn();
      final Object value = typeHandler.getResult(rs, column);
      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
  }

  protected Object createPrimitiveResultObject(ResultSet rs, ResultMap resultMap) throws SQLException {
    final Class resultType = resultMap.getType();
    final String columnName;
    if (resultMap.getResultMappings().size() > 0) {
      final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
      final ResultMapping mapping = resultMappingList.get(0);
      columnName = mapping.getColumn();
    } else {
      final ResultSetMetaData rsmd = rs.getMetaData();
      columnName = configuration.isUseColumnLabel() ? rsmd.getColumnLabel(1) : rsmd.getColumnName(1);
    }
    final TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(resultType);
    return typeHandler.getResult(rs, columnName);
  }

  //
  // NESTED QUERY
  //

  protected Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader) throws SQLException {
    final String nestedQueryId = propertyMapping.getNestedQueryId();
    final String property = propertyMapping.getProperty();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping, nestedQueryParameterType);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT);
      if (executor.isCached(nestedQuery, key)) {
        executor.deferLoad(nestedQuery, metaResultObject, property, key);
      } else {
        final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, propertyMapping.getJavaType());
        if (configuration.isLazyLoadingEnabled()) {
          lazyLoader.addLoader(property, metaResultObject, resultLoader);
        } else {
          value = resultLoader.loadResult();
        }
      }
    }
    return value;
  }

  protected Object prepareParameterForNestedQuery(ResultSet rs, ResultMapping resultMapping, Class parameterType) throws SQLException {
    if (resultMapping.isCompositeResult()) {
      return prepareCompositeKeyParameter(rs, resultMapping, parameterType);
    } else {
      return prepareSimpleKeyParameter(rs, resultMapping, parameterType);
    }
  }

  protected Object prepareSimpleKeyParameter(ResultSet rs, ResultMapping resultMapping, Class parameterType) throws SQLException {
    final TypeHandler typeHandler;
    if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
      typeHandler = typeHandlerRegistry.getTypeHandler(parameterType);
    } else {
      typeHandler = typeHandlerRegistry.getUnknownTypeHandler();
    }
    return typeHandler.getResult(rs, resultMapping.getColumn());
  }

  protected Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class parameterType) throws SQLException {
    final Object parameterObject = instantiateParameterObject(parameterType);
    final MetaObject metaObject = configuration.newMetaObject(parameterObject);
    for (ResultMapping innerResultMapping : resultMapping.getComposites()) {
      final Class propType = metaObject.getSetterType(innerResultMapping.getProperty());
      final TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(propType);
      final Object propValue = typeHandler.getResult(rs, innerResultMapping.getColumn());
      metaObject.setValue(innerResultMapping.getProperty(), propValue);
    }
    return parameterObject;
  }

  protected Object instantiateParameterObject(Class parameterType) {
    if (parameterType == null) {
      return new HashMap();
    } else {
      return objectFactory.create(parameterType);
    }
  }

  //
  // DISCRIMINATOR([美][dɪˈskrɪməˌnet] 歧视；区别；辨出)
  /**
   * 处理resultMap配置中的discriminator,如果没有discriminator，返回原来的resultMap
   */
  public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap) throws SQLException {
    Set<String> pastDiscriminators = new HashSet<String>();
    Discriminator discriminator = resultMap.getDiscriminator();
    while (discriminator != null) {
      final Object value = getDiscriminatorValue(rs, discriminator);
      final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value));
      if (configuration.hasResultMap(discriminatedMapId)) {
        resultMap = configuration.getResultMap(discriminatedMapId);
        Discriminator lastDiscriminator = discriminator;
        discriminator = resultMap.getDiscriminator();
        if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
          break;
        }
      } else {
        break;
      }
    }
    return resultMap;
  }

  protected Object getDiscriminatorValue(ResultSet rs, Discriminator discriminator) throws SQLException {
    final ResultMapping resultMapping = discriminator.getResultMapping();
    final TypeHandler typeHandler = resultMapping.getTypeHandler();
    if (typeHandler != null) {
      return typeHandler.getResult(rs, resultMapping.getColumn());
    } else {
      throw new ExecutorException("No type handler could be found to map the property '" + resultMapping.getProperty() + "' to the column '" + resultMapping.getColumn() + "'.  One or both of the types, or the combination of types is not supported.");
    }
  }

}