package org.apache.ibatis.type;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;

/**
 * 维护管理alias 
 */
public class TypeAliasRegistry {


  private final HashMap<String, Class> TYPE_ALIASES = new HashMap<String, Class>();

  /**
   * 默认注册mybatis自己定义的一些type alias
   */
  public TypeAliasRegistry() {
    registerAlias("string", String.class);

    registerAlias("byte", Byte.class);
    registerAlias("long", Long.class);
    registerAlias("short", Short.class);
    registerAlias("int", Integer.class);
    registerAlias("integer", Integer.class);
    registerAlias("double", Double.class);
    registerAlias("float", Float.class);
    registerAlias("boolean", Boolean.class);

    registerAlias("byte[]", Byte[].class);
    registerAlias("long[]", Long[].class);
    registerAlias("short[]", Short[].class);
    registerAlias("int[]", Integer[].class);
    registerAlias("integer[]", Integer[].class);
    registerAlias("double[]", Double[].class);
    registerAlias("float[]", Float[].class);
    registerAlias("boolean[]", Boolean[].class);

    registerAlias("_byte", byte.class);
    registerAlias("_long", long.class);
    registerAlias("_short", short.class);
    registerAlias("_int", int.class);
    registerAlias("_integer", int.class);
    registerAlias("_double", double.class);
    registerAlias("_float", float.class);
    registerAlias("_boolean", boolean.class);

    registerAlias("_byte[]", byte[].class);
    registerAlias("_long[]", long[].class);
    registerAlias("_short[]", short[].class);
    registerAlias("_int[]", int[].class);
    registerAlias("_integer[]", int[].class);
    registerAlias("_double[]", double[].class);
    registerAlias("_float[]", float[].class);
    registerAlias("_boolean[]", boolean[].class);

    registerAlias("date", Date.class);
    registerAlias("decimal", BigDecimal.class);
    registerAlias("bigdecimal", BigDecimal.class);
    registerAlias("object", Object.class);

    registerAlias("date[]", Date[].class);
    registerAlias("decimal[]", BigDecimal[].class);
    registerAlias("bigdecimal[]", BigDecimal[].class);
    registerAlias("object[]", Object[].class);

    registerAlias("map", Map.class);
    registerAlias("hashmap", HashMap.class);
    registerAlias("list", List.class);
    registerAlias("arraylist", ArrayList.class);
    registerAlias("collection", Collection.class);
    registerAlias("iterator", Iterator.class);

    registerAlias("ResultSet", ResultSet.class);
  }

  public Class resolveAlias(String string) {
    try {
      if (string == null) return null;
      String key = string.toLowerCase();
      Class value;
      if (TYPE_ALIASES.containsKey(key)) {
        value = TYPE_ALIASES.get(key);
      } else {
        value = Resources.classForName(string);
      }
      return value;
    } catch (ClassNotFoundException e) {
      throw new TypeException("Could not resolve type alias '" +string+ "'.  Cause: " + e, e);
    }
  }

  public void registerAliases(String packageName){
    registerAliases(packageName, Object.class);
  }

  public void registerAliases(String packageName, Class superType){
    ResolverUtil<Class> resolverUtil = new ResolverUtil<Class>();
    //找出packageName对应的路径中，isAssignableFrom superType的java类
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class>> typeSet = resolverUtil.getClasses();
    for(Class type : typeSet){
      //Ignore inner classes and interfaces (including package-info.java)
      //忽略匿名类和接口
      if (!type.isAnonymousClass() && !type.isInterface()) {
        registerAlias(type);
      }
    }
  }

  /**
   * <li>优先使用{@link org.apache.ibatis.type.Alias} 配置的注解作alias
   * <li>没有注解的话，使用Class#getSimpleName作alias
   */
  public void registerAlias(Class type) {
    String alias = type.getSimpleName();
    Alias aliasAnnotation = (Alias) type.getAnnotation(Alias.class);
    if (aliasAnnotation != null) {
      alias = aliasAnnotation.value();
    } 
    registerAlias(alias, type);
  }

  public void registerAlias(String alias, Class value) {
    assert alias != null;
    String key = alias.toLowerCase();
    if (TYPE_ALIASES.containsKey(key) && !TYPE_ALIASES.get(key).equals(value.getName()) && TYPE_ALIASES.get(alias) != null) {
      if (!value.equals(TYPE_ALIASES.get(alias))) {
        throw new TypeException("The alias '" + alias + "' is already mapped to the value '" + TYPE_ALIASES.get(alias).getName() + "'.");
      }
    }
    TYPE_ALIASES.put(key, value);
  }

  public void registerAlias(String alias, String value) {
    try {
      registerAlias(alias, Resources.classForName(value));
    } catch (ClassNotFoundException e) {
      throw new TypeException("Error registering type alias "+alias+" for "+value+". Cause: " + e, e);
    }
  }

}
