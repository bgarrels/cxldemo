/*
 * This class autogenerated by com.mchange.v2.c3p0.codegen.BeangenDataSourceGenerator.
 * DO NOT HAND EDIT!
 */

package com.mchange.v2.c3p0.impl;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.ConnectionCustomizer;
import com.mchange.v2.c3p0.cfg.C3P0Config;
import com.mchange.v2.naming.JavaBeanObjectFactory;
import com.mchange.v2.naming.JavaBeanReferenceMaker;
import com.mchange.v2.naming.ReferenceIndirector;
import com.mchange.v2.naming.ReferenceMaker;
import com.mchange.v2.ser.IndirectlySerialized;
import com.mchange.v2.ser.Indirector;
import com.mchange.v2.ser.SerializableUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

public abstract class WrapperConnectionPoolDataSourceBase extends IdentityTokenResolvable implements Referenceable, Serializable
{
	protected PropertyChangeSupport pcs = new PropertyChangeSupport( this );
	
	protected PropertyChangeSupport getPropertyChangeSupport()
	{ return pcs; }
	protected VetoableChangeSupport vcs = new VetoableChangeSupport( this );
	
	protected VetoableChangeSupport getVetoableChangeSupport()
	{ return vcs; }
	private int acquireIncrement = C3P0Config.initializeIntPropertyVar("acquireIncrement", C3P0Defaults.acquireIncrement());
	private int acquireRetryAttempts = C3P0Config.initializeIntPropertyVar("acquireRetryAttempts", C3P0Defaults.acquireRetryAttempts());
	private int acquireRetryDelay = C3P0Config.initializeIntPropertyVar("acquireRetryDelay", C3P0Defaults.acquireRetryDelay());
	private boolean autoCommitOnClose = C3P0Config.initializeBooleanPropertyVar("autoCommitOnClose", C3P0Defaults.autoCommitOnClose());
	private String automaticTestTable = C3P0Config.initializeStringPropertyVar("automaticTestTable", C3P0Defaults.automaticTestTable());
	private boolean breakAfterAcquireFailure = C3P0Config.initializeBooleanPropertyVar("breakAfterAcquireFailure", C3P0Defaults.breakAfterAcquireFailure());
	private int checkoutTimeout = C3P0Config.initializeIntPropertyVar("checkoutTimeout", C3P0Defaults.checkoutTimeout());
	private String connectionCustomizerClassName = C3P0Config.initializeStringPropertyVar("connectionCustomizerClassName", C3P0Defaults.connectionCustomizerClassName());
	private String connectionTesterClassName = C3P0Config.initializeStringPropertyVar("connectionTesterClassName", C3P0Defaults.connectionTesterClassName());
	private boolean debugUnreturnedConnectionStackTraces = C3P0Config.initializeBooleanPropertyVar("debugUnreturnedConnectionStackTraces", C3P0Defaults.debugUnreturnedConnectionStackTraces());
	private String factoryClassLocation = C3P0Config.initializeStringPropertyVar("factoryClassLocation", C3P0Defaults.factoryClassLocation());
	private boolean forceIgnoreUnresolvedTransactions = C3P0Config.initializeBooleanPropertyVar("forceIgnoreUnresolvedTransactions", C3P0Defaults.forceIgnoreUnresolvedTransactions());
	private String identityToken;
	private int idleConnectionTestPeriod = C3P0Config.initializeIntPropertyVar("idleConnectionTestPeriod", C3P0Defaults.idleConnectionTestPeriod());
	private int initialPoolSize = C3P0Config.initializeIntPropertyVar("initialPoolSize", C3P0Defaults.initialPoolSize());
	private int maxAdministrativeTaskTime = C3P0Config.initializeIntPropertyVar("maxAdministrativeTaskTime", C3P0Defaults.maxAdministrativeTaskTime());
	private int maxConnectionAge = C3P0Config.initializeIntPropertyVar("maxConnectionAge", C3P0Defaults.maxConnectionAge());
	private int maxIdleTime = C3P0Config.initializeIntPropertyVar("maxIdleTime", C3P0Defaults.maxIdleTime());
	private int maxIdleTimeExcessConnections = C3P0Config.initializeIntPropertyVar("maxIdleTimeExcessConnections", C3P0Defaults.maxIdleTimeExcessConnections());
	private int maxPoolSize = C3P0Config.initializeIntPropertyVar("maxPoolSize", C3P0Defaults.maxPoolSize());
	private int maxStatements = C3P0Config.initializeIntPropertyVar("maxStatements", C3P0Defaults.maxStatements());
	private int maxStatementsPerConnection = C3P0Config.initializeIntPropertyVar("maxStatementsPerConnection", C3P0Defaults.maxStatementsPerConnection());
	private int minPoolSize = C3P0Config.initializeIntPropertyVar("minPoolSize", C3P0Defaults.minPoolSize());
	private DataSource nestedDataSource;
	private String overrideDefaultPassword = C3P0Config.initializeStringPropertyVar("overrideDefaultPassword", C3P0Defaults.overrideDefaultPassword());
	private String overrideDefaultUser = C3P0Config.initializeStringPropertyVar("overrideDefaultUser", C3P0Defaults.overrideDefaultUser());
	private String preferredTestQuery = C3P0Config.initializeStringPropertyVar("preferredTestQuery", C3P0Defaults.preferredTestQuery());
	private int propertyCycle = C3P0Config.initializeIntPropertyVar("propertyCycle", C3P0Defaults.propertyCycle());
	private boolean testConnectionOnCheckin = C3P0Config.initializeBooleanPropertyVar("testConnectionOnCheckin", C3P0Defaults.testConnectionOnCheckin());
	private boolean testConnectionOnCheckout = C3P0Config.initializeBooleanPropertyVar("testConnectionOnCheckout", C3P0Defaults.testConnectionOnCheckout());
	private int unreturnedConnectionTimeout = C3P0Config.initializeIntPropertyVar("unreturnedConnectionTimeout", C3P0Defaults.unreturnedConnectionTimeout());
	private String userOverridesAsString = C3P0Config.initializeUserOverridesAsString();
	private boolean usesTraditionalReflectiveProxies = C3P0Config.initializeBooleanPropertyVar("usesTraditionalReflectiveProxies", C3P0Defaults.usesTraditionalReflectiveProxies());
	
	public synchronized int getAcquireIncrement()
	{ return acquireIncrement; }
	
	public synchronized void setAcquireIncrement( int acquireIncrement )
	{
		this.acquireIncrement = acquireIncrement;
	}
	
	public synchronized int getAcquireRetryAttempts()
	{ return acquireRetryAttempts; }
	
	public synchronized void setAcquireRetryAttempts( int acquireRetryAttempts )
	{
		this.acquireRetryAttempts = acquireRetryAttempts;
	}
	
	public synchronized int getAcquireRetryDelay()
	{ return acquireRetryDelay; }
	
	public synchronized void setAcquireRetryDelay( int acquireRetryDelay )
	{
		this.acquireRetryDelay = acquireRetryDelay;
	}
	
	public synchronized boolean isAutoCommitOnClose()
	{ return autoCommitOnClose; }
	
	public synchronized void setAutoCommitOnClose( boolean autoCommitOnClose )
	{
		this.autoCommitOnClose = autoCommitOnClose;
	}
	
	public synchronized String getAutomaticTestTable()
	{ return automaticTestTable; }
	
	public synchronized void setAutomaticTestTable( String automaticTestTable )
	{
		this.automaticTestTable = automaticTestTable;
	}
	
	public synchronized boolean isBreakAfterAcquireFailure()
	{ return breakAfterAcquireFailure; }
	
	public synchronized void setBreakAfterAcquireFailure( boolean breakAfterAcquireFailure )
	{
		this.breakAfterAcquireFailure = breakAfterAcquireFailure;
	}
	
	public synchronized int getCheckoutTimeout()
	{ return checkoutTimeout; }
	
	public synchronized void setCheckoutTimeout( int checkoutTimeout )
	{
		this.checkoutTimeout = checkoutTimeout;
	}
	
	public synchronized String getConnectionCustomizerClassName()
	{ return connectionCustomizerClassName; }
	
	public synchronized void setConnectionCustomizerClassName( String connectionCustomizerClassName )
	{
		this.connectionCustomizerClassName = connectionCustomizerClassName;
	}
	
	public synchronized String getConnectionTesterClassName()
	{ return connectionTesterClassName; }
	
	public synchronized void setConnectionTesterClassName( String connectionTesterClassName ) throws PropertyVetoException
	{
		String oldVal = this.connectionTesterClassName;
		if ( ! eqOrBothNull( oldVal, connectionTesterClassName ) )
			vcs.fireVetoableChange( "connectionTesterClassName", oldVal, connectionTesterClassName );
		this.connectionTesterClassName = connectionTesterClassName;
	}
	
	public synchronized boolean isDebugUnreturnedConnectionStackTraces()
	{ return debugUnreturnedConnectionStackTraces; }
	
	public synchronized void setDebugUnreturnedConnectionStackTraces( boolean debugUnreturnedConnectionStackTraces )
	{
		this.debugUnreturnedConnectionStackTraces = debugUnreturnedConnectionStackTraces;
	}
	
	public synchronized String getFactoryClassLocation()
	{ return factoryClassLocation; }
	
	public synchronized void setFactoryClassLocation( String factoryClassLocation )
	{
		this.factoryClassLocation = factoryClassLocation;
	}
	
	public synchronized boolean isForceIgnoreUnresolvedTransactions()
	{ return forceIgnoreUnresolvedTransactions; }
	
	public synchronized void setForceIgnoreUnresolvedTransactions( boolean forceIgnoreUnresolvedTransactions )
	{
		this.forceIgnoreUnresolvedTransactions = forceIgnoreUnresolvedTransactions;
	}
	
	public synchronized String getIdentityToken()
	{ return identityToken; }
	
	public synchronized void setIdentityToken( String identityToken )
	{
		String oldVal = this.identityToken;
		this.identityToken = identityToken;
		if ( ! eqOrBothNull( oldVal, identityToken ) )
			pcs.firePropertyChange( "identityToken", oldVal, identityToken );
	}
	
	public synchronized int getIdleConnectionTestPeriod()
	{ return idleConnectionTestPeriod; }
	
	public synchronized void setIdleConnectionTestPeriod( int idleConnectionTestPeriod )
	{
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
	}
	
	public synchronized int getInitialPoolSize()
	{ return initialPoolSize; }
	
	public synchronized void setInitialPoolSize( int initialPoolSize )
	{
		this.initialPoolSize = initialPoolSize;
	}
	
	public synchronized int getMaxAdministrativeTaskTime()
	{ return maxAdministrativeTaskTime; }
	
	public synchronized void setMaxAdministrativeTaskTime( int maxAdministrativeTaskTime )
	{
		this.maxAdministrativeTaskTime = maxAdministrativeTaskTime;
	}
	
	public synchronized int getMaxConnectionAge()
	{ return maxConnectionAge; }
	
	public synchronized void setMaxConnectionAge( int maxConnectionAge )
	{
		this.maxConnectionAge = maxConnectionAge;
	}
	
	public synchronized int getMaxIdleTime()
	{ return maxIdleTime; }
	
	public synchronized void setMaxIdleTime( int maxIdleTime )
	{
		this.maxIdleTime = maxIdleTime;
	}
	
	public synchronized int getMaxIdleTimeExcessConnections()
	{ return maxIdleTimeExcessConnections; }
	
	public synchronized void setMaxIdleTimeExcessConnections( int maxIdleTimeExcessConnections )
	{
		this.maxIdleTimeExcessConnections = maxIdleTimeExcessConnections;
	}
	
	public synchronized int getMaxPoolSize()
	{ return maxPoolSize; }
	
	public synchronized void setMaxPoolSize( int maxPoolSize )
	{
		this.maxPoolSize = maxPoolSize;
	}
	
	public synchronized int getMaxStatements()
	{ return maxStatements; }
	
	public synchronized void setMaxStatements( int maxStatements )
	{
		this.maxStatements = maxStatements;
	}
	
	public synchronized int getMaxStatementsPerConnection()
	{ return maxStatementsPerConnection; }
	
	public synchronized void setMaxStatementsPerConnection( int maxStatementsPerConnection )
	{
		this.maxStatementsPerConnection = maxStatementsPerConnection;
	}
	
	public synchronized int getMinPoolSize()
	{ return minPoolSize; }
	
	public synchronized void setMinPoolSize( int minPoolSize )
	{
		this.minPoolSize = minPoolSize;
	}
	
	public synchronized DataSource getNestedDataSource()
	{ return nestedDataSource; }
	
	public synchronized void setNestedDataSource( DataSource nestedDataSource )
	{
		DataSource oldVal = this.nestedDataSource;
		this.nestedDataSource = nestedDataSource;
		if ( ! eqOrBothNull( oldVal, nestedDataSource ) )
			pcs.firePropertyChange( "nestedDataSource", oldVal, nestedDataSource );
	}
	
	public synchronized String getOverrideDefaultPassword()
	{ return overrideDefaultPassword; }
	
	public synchronized void setOverrideDefaultPassword( String overrideDefaultPassword )
	{
		this.overrideDefaultPassword = overrideDefaultPassword;
	}
	
	public synchronized String getOverrideDefaultUser()
	{ return overrideDefaultUser; }
	
	public synchronized void setOverrideDefaultUser( String overrideDefaultUser )
	{
		this.overrideDefaultUser = overrideDefaultUser;
	}
	
	public synchronized String getPreferredTestQuery()
	{ return preferredTestQuery; }
	
	public synchronized void setPreferredTestQuery( String preferredTestQuery )
	{
		this.preferredTestQuery = preferredTestQuery;
	}
	
	public synchronized int getPropertyCycle()
	{ return propertyCycle; }
	
	public synchronized void setPropertyCycle( int propertyCycle )
	{
		this.propertyCycle = propertyCycle;
	}
	
	public synchronized boolean isTestConnectionOnCheckin()
	{ return testConnectionOnCheckin; }
	
	public synchronized void setTestConnectionOnCheckin( boolean testConnectionOnCheckin )
	{
		this.testConnectionOnCheckin = testConnectionOnCheckin;
	}
	
	public synchronized boolean isTestConnectionOnCheckout()
	{ return testConnectionOnCheckout; }
	
	public synchronized void setTestConnectionOnCheckout( boolean testConnectionOnCheckout )
	{
		this.testConnectionOnCheckout = testConnectionOnCheckout;
	}
	
	public synchronized int getUnreturnedConnectionTimeout()
	{ return unreturnedConnectionTimeout; }
	
	public synchronized void setUnreturnedConnectionTimeout( int unreturnedConnectionTimeout )
	{
		this.unreturnedConnectionTimeout = unreturnedConnectionTimeout;
	}
	
	public synchronized String getUserOverridesAsString()
	{ return userOverridesAsString; }
	
	public synchronized void setUserOverridesAsString( String userOverridesAsString ) throws PropertyVetoException
	{
		String oldVal = this.userOverridesAsString;
		if ( ! eqOrBothNull( oldVal, userOverridesAsString ) )
			vcs.fireVetoableChange( "userOverridesAsString", oldVal, userOverridesAsString );
		this.userOverridesAsString = userOverridesAsString;
	}
	
	public synchronized boolean isUsesTraditionalReflectiveProxies()
	{ return usesTraditionalReflectiveProxies; }
	
	public synchronized void setUsesTraditionalReflectiveProxies( boolean usesTraditionalReflectiveProxies )
	{
		this.usesTraditionalReflectiveProxies = usesTraditionalReflectiveProxies;
	}
	
	public void addPropertyChangeListener( PropertyChangeListener pcl )
	{ pcs.addPropertyChangeListener( pcl ); }
	
	public void addPropertyChangeListener( String propName, PropertyChangeListener pcl )
	{ pcs.addPropertyChangeListener( propName, pcl ); }
	
	public void removePropertyChangeListener( PropertyChangeListener pcl )
	{ pcs.removePropertyChangeListener( pcl ); }
	
	public void removePropertyChangeListener( String propName, PropertyChangeListener pcl )
	{ pcs.removePropertyChangeListener( propName, pcl ); }
	
	
	public void addVetoableChangeListener( VetoableChangeListener vcl )
	{ vcs.addVetoableChangeListener( vcl ); }
	
	public void removeVetoableChangeListener( VetoableChangeListener vcl )
	{ vcs.removeVetoableChangeListener( vcl ); }
	
	private boolean eqOrBothNull( Object a, Object b )
	{
		return
			a == b ||
			(a != null && a.equals(b));
	}
	
	private static final long serialVersionUID = 1;
	private static final short VERSION = 0x0001;
	
	private void writeObject( ObjectOutputStream oos ) throws IOException
	{
		oos.writeShort( VERSION );
		oos.writeInt(acquireIncrement);
		oos.writeInt(acquireRetryAttempts);
		oos.writeInt(acquireRetryDelay);
		oos.writeBoolean(autoCommitOnClose);
		oos.writeObject( automaticTestTable );
		oos.writeBoolean(breakAfterAcquireFailure);
		oos.writeInt(checkoutTimeout);
		oos.writeObject( connectionCustomizerClassName );
		oos.writeObject( connectionTesterClassName );
		oos.writeBoolean(debugUnreturnedConnectionStackTraces);
		oos.writeObject( factoryClassLocation );
		oos.writeBoolean(forceIgnoreUnresolvedTransactions);
		oos.writeObject( identityToken );
		oos.writeInt(idleConnectionTestPeriod);
		oos.writeInt(initialPoolSize);
		oos.writeInt(maxAdministrativeTaskTime);
		oos.writeInt(maxConnectionAge);
		oos.writeInt(maxIdleTime);
		oos.writeInt(maxIdleTimeExcessConnections);
		oos.writeInt(maxPoolSize);
		oos.writeInt(maxStatements);
		oos.writeInt(maxStatementsPerConnection);
		oos.writeInt(minPoolSize);
		try
		{
			//test serialize
			SerializableUtils.toByteArray(nestedDataSource);
			oos.writeObject( nestedDataSource );
		}
		catch (NotSerializableException nse)
		{
			try
			{
				Indirector indirector = new com.mchange.v2.naming.ReferenceIndirector();
				oos.writeObject( indirector.indirectForm( nestedDataSource ) );
			}
			catch (IOException indirectionIOException)
			{ throw indirectionIOException; }
			catch (Exception indirectionOtherException)
			{ throw new IOException("Problem indirectly serializing nestedDataSource: " + indirectionOtherException.toString() ); }
		}
		oos.writeObject( overrideDefaultPassword );
		oos.writeObject( overrideDefaultUser );
		oos.writeObject( preferredTestQuery );
		oos.writeInt(propertyCycle);
		oos.writeBoolean(testConnectionOnCheckin);
		oos.writeBoolean(testConnectionOnCheckout);
		oos.writeInt(unreturnedConnectionTimeout);
		oos.writeObject( userOverridesAsString );
		oos.writeBoolean(usesTraditionalReflectiveProxies);
	}
	
	private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		short version = ois.readShort();
		switch (version)
		{
			case VERSION:
				this.acquireIncrement = ois.readInt();
				this.acquireRetryAttempts = ois.readInt();
				this.acquireRetryDelay = ois.readInt();
				this.autoCommitOnClose = ois.readBoolean();
				this.automaticTestTable = (String) ois.readObject();
				this.breakAfterAcquireFailure = ois.readBoolean();
				this.checkoutTimeout = ois.readInt();
				this.connectionCustomizerClassName = (String) ois.readObject();
				this.connectionTesterClassName = (String) ois.readObject();
				this.debugUnreturnedConnectionStackTraces = ois.readBoolean();
				this.factoryClassLocation = (String) ois.readObject();
				this.forceIgnoreUnresolvedTransactions = ois.readBoolean();
				this.identityToken = (String) ois.readObject();
				this.idleConnectionTestPeriod = ois.readInt();
				this.initialPoolSize = ois.readInt();
				this.maxAdministrativeTaskTime = ois.readInt();
				this.maxConnectionAge = ois.readInt();
				this.maxIdleTime = ois.readInt();
				this.maxIdleTimeExcessConnections = ois.readInt();
				this.maxPoolSize = ois.readInt();
				this.maxStatements = ois.readInt();
				this.maxStatementsPerConnection = ois.readInt();
				this.minPoolSize = ois.readInt();
				Object o = ois.readObject();
				if (o instanceof IndirectlySerialized) o = ((IndirectlySerialized) o).getObject();
				this.nestedDataSource = (DataSource) o;
				this.overrideDefaultPassword = (String) ois.readObject();
				this.overrideDefaultUser = (String) ois.readObject();
				this.preferredTestQuery = (String) ois.readObject();
				this.propertyCycle = ois.readInt();
				this.testConnectionOnCheckin = ois.readBoolean();
				this.testConnectionOnCheckout = ois.readBoolean();
				this.unreturnedConnectionTimeout = ois.readInt();
				this.userOverridesAsString = (String) ois.readObject();
				this.usesTraditionalReflectiveProxies = ois.readBoolean();
				this.pcs = new PropertyChangeSupport( this );
				this.vcs = new VetoableChangeSupport( this );
				break;
			default:
				throw new IOException("Unsupported Serialized Version: " + version);
		}
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( super.toString() );
		sb.append(" [ ");
		sb.append( "acquireIncrement -> " + acquireIncrement );
		sb.append( ", ");
		sb.append( "acquireRetryAttempts -> " + acquireRetryAttempts );
		sb.append( ", ");
		sb.append( "acquireRetryDelay -> " + acquireRetryDelay );
		sb.append( ", ");
		sb.append( "autoCommitOnClose -> " + autoCommitOnClose );
		sb.append( ", ");
		sb.append( "automaticTestTable -> " + automaticTestTable );
		sb.append( ", ");
		sb.append( "breakAfterAcquireFailure -> " + breakAfterAcquireFailure );
		sb.append( ", ");
		sb.append( "checkoutTimeout -> " + checkoutTimeout );
		sb.append( ", ");
		sb.append( "connectionCustomizerClassName -> " + connectionCustomizerClassName );
		sb.append( ", ");
		sb.append( "connectionTesterClassName -> " + connectionTesterClassName );
		sb.append( ", ");
		sb.append( "debugUnreturnedConnectionStackTraces -> " + debugUnreturnedConnectionStackTraces );
		sb.append( ", ");
		sb.append( "factoryClassLocation -> " + factoryClassLocation );
		sb.append( ", ");
		sb.append( "forceIgnoreUnresolvedTransactions -> " + forceIgnoreUnresolvedTransactions );
		sb.append( ", ");
		sb.append( "identityToken -> " + identityToken );
		sb.append( ", ");
		sb.append( "idleConnectionTestPeriod -> " + idleConnectionTestPeriod );
		sb.append( ", ");
		sb.append( "initialPoolSize -> " + initialPoolSize );
		sb.append( ", ");
		sb.append( "maxAdministrativeTaskTime -> " + maxAdministrativeTaskTime );
		sb.append( ", ");
		sb.append( "maxConnectionAge -> " + maxConnectionAge );
		sb.append( ", ");
		sb.append( "maxIdleTime -> " + maxIdleTime );
		sb.append( ", ");
		sb.append( "maxIdleTimeExcessConnections -> " + maxIdleTimeExcessConnections );
		sb.append( ", ");
		sb.append( "maxPoolSize -> " + maxPoolSize );
		sb.append( ", ");
		sb.append( "maxStatements -> " + maxStatements );
		sb.append( ", ");
		sb.append( "maxStatementsPerConnection -> " + maxStatementsPerConnection );
		sb.append( ", ");
		sb.append( "minPoolSize -> " + minPoolSize );
		sb.append( ", ");
		sb.append( "nestedDataSource -> " + nestedDataSource );
		sb.append( ", ");
		sb.append( "preferredTestQuery -> " + preferredTestQuery );
		sb.append( ", ");
		sb.append( "propertyCycle -> " + propertyCycle );
		sb.append( ", ");
		sb.append( "testConnectionOnCheckin -> " + testConnectionOnCheckin );
		sb.append( ", ");
		sb.append( "testConnectionOnCheckout -> " + testConnectionOnCheckout );
		sb.append( ", ");
		sb.append( "unreturnedConnectionTimeout -> " + unreturnedConnectionTimeout );
		sb.append( ", ");
		sb.append( "usesTraditionalReflectiveProxies -> " + usesTraditionalReflectiveProxies );
		
		String extraToStringInfo = this.extraToStringInfo();
		if (extraToStringInfo != null)
			sb.append( extraToStringInfo );
		sb.append(" ]");
		return sb.toString();
	}
	
	protected String extraToStringInfo()
	{ return null; }
	
	final static JavaBeanReferenceMaker referenceMaker = new com.mchange.v2.naming.JavaBeanReferenceMaker();
	
	static
	{
		referenceMaker.setFactoryClassName( "com.mchange.v2.c3p0.impl.C3P0JavaBeanObjectFactory" );
		referenceMaker.addReferenceProperty("acquireIncrement");
		referenceMaker.addReferenceProperty("acquireRetryAttempts");
		referenceMaker.addReferenceProperty("acquireRetryDelay");
		referenceMaker.addReferenceProperty("autoCommitOnClose");
		referenceMaker.addReferenceProperty("automaticTestTable");
		referenceMaker.addReferenceProperty("breakAfterAcquireFailure");
		referenceMaker.addReferenceProperty("checkoutTimeout");
		referenceMaker.addReferenceProperty("connectionCustomizerClassName");
		referenceMaker.addReferenceProperty("connectionTesterClassName");
		referenceMaker.addReferenceProperty("debugUnreturnedConnectionStackTraces");
		referenceMaker.addReferenceProperty("factoryClassLocation");
		referenceMaker.addReferenceProperty("forceIgnoreUnresolvedTransactions");
		referenceMaker.addReferenceProperty("identityToken");
		referenceMaker.addReferenceProperty("idleConnectionTestPeriod");
		referenceMaker.addReferenceProperty("initialPoolSize");
		referenceMaker.addReferenceProperty("maxAdministrativeTaskTime");
		referenceMaker.addReferenceProperty("maxConnectionAge");
		referenceMaker.addReferenceProperty("maxIdleTime");
		referenceMaker.addReferenceProperty("maxIdleTimeExcessConnections");
		referenceMaker.addReferenceProperty("maxPoolSize");
		referenceMaker.addReferenceProperty("maxStatements");
		referenceMaker.addReferenceProperty("maxStatementsPerConnection");
		referenceMaker.addReferenceProperty("minPoolSize");
		referenceMaker.addReferenceProperty("nestedDataSource");
		referenceMaker.addReferenceProperty("overrideDefaultPassword");
		referenceMaker.addReferenceProperty("overrideDefaultUser");
		referenceMaker.addReferenceProperty("preferredTestQuery");
		referenceMaker.addReferenceProperty("propertyCycle");
		referenceMaker.addReferenceProperty("testConnectionOnCheckin");
		referenceMaker.addReferenceProperty("testConnectionOnCheckout");
		referenceMaker.addReferenceProperty("unreturnedConnectionTimeout");
		referenceMaker.addReferenceProperty("userOverridesAsString");
		referenceMaker.addReferenceProperty("usesTraditionalReflectiveProxies");
	}
	
	public Reference getReference() throws NamingException
	{
		return referenceMaker.createReference( this );
	}
	
	private WrapperConnectionPoolDataSourceBase()
	{}
	
	public WrapperConnectionPoolDataSourceBase( boolean autoregister )
	{
		if (autoregister)
		{
			this.identityToken = C3P0ImplUtils.allocateIdentityToken( this );
			C3P0Registry.reregister( this );
		}
	}
	
	protected abstract PooledConnection getPooledConnection( ConnectionCustomizer cc, String idt) throws SQLException;
	protected abstract PooledConnection getPooledConnection(String user, String password, ConnectionCustomizer cc, String idt) throws SQLException;
}
