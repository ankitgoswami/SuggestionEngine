package com.se.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.IOUtils;
import org.apache.mahout.cf.taste.impl.model.jdbc.AbstractJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC-based {@link DataModel} implementations
 * 
 * @author ankit
 * 
 */
public class JDBCDataModelImpl extends AbstractJDBCDataModel {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractJDBCDataModel.class);

	private static final String ratingTable = "ratings";
	private static final String userIDColumn = "user_id";
	private static final String itemIDColumn = "item_id";
	private static final String preferenceColumn = "rating";
	private static final String getPreferenceSQL = "select "+userIDColumn+","+itemIDColumn+","+preferenceColumn+" from "+ratingTable+" where "+userIDColumn+"=? and "+itemIDColumn+"=?";
	private static final String getUserSQL = "select "+userIDColumn+","+itemIDColumn+","+preferenceColumn+" from "+ratingTable+" where "+userIDColumn+"=?";
	private static final String getAllUsersSQL = "select "+userIDColumn+", "+itemIDColumn+", "+preferenceColumn+" from "+ratingTable; // hoo boy
	private static final String getNumItemsSQL = "select count(distinct "+itemIDColumn+") from "+ratingTable;
	private static final String getNumUsersSQL = "select count(distinct "+userIDColumn+") from "+ratingTable;
	private static final String setPreferenceSQL = "insert into "+ratingTable+"("+userIDColumn+", "+itemIDColumn+", "+preferenceColumn+") values (?,?,?)";
	private static final String removePreferenceSQL = "delete from "+ratingTable+" where "+userIDColumn+"=?, "+itemIDColumn+"=?";
	private static final String getUsersSQL = "select distinct "+userIDColumn+" from "+ratingTable;
	private static final String getItemsSQL = "select distinct "+itemIDColumn+" from "+ratingTable;
	private static final String getPrefsForItemSQL = "select "+preferenceColumn+" from "+ratingTable+" where "+itemIDColumn+"=?";
	private static final String getNumPreferenceForItemSQL = "select count("+preferenceColumn+") from "+ratingTable+" where "+itemIDColumn+"=?";
	private static final String getNumPreferenceForItemsSQL = "select count("+preferenceColumn+") from "+ratingTable+" where "+itemIDColumn+"=? or "+itemIDColumn+"=?";

	private DataSource dataSource; 

	public JDBCDataModelImpl(DataSource dataSource) {
		super(dataSource, ratingTable, userIDColumn, itemIDColumn,
				preferenceColumn, getPreferenceSQL, getUserSQL, getAllUsersSQL,
				getNumItemsSQL, getNumUsersSQL, setPreferenceSQL,
				removePreferenceSQL, getUsersSQL, getItemsSQL,
				getPrefsForItemSQL, getNumPreferenceForItemSQL,
				getNumPreferenceForItemsSQL);
		this.dataSource = dataSource;
	}
	
	/**
	 * added this as the implementation in AbstractJDBCDataModel requires to set 3 values for some reason
	 */
	@Override
	public void setPreference(long userID, long itemID, float value)
			throws TasteException {
		if (Float.isNaN(value)) {
			throw new IllegalArgumentException("Invalid value: " + value);
		}

		log.debug("Setting preference for user {}, item {}", userID, itemID);

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(setPreferenceSQL);
			stmt.setLong(1, userID);
			stmt.setLong(2, itemID);
			stmt.setDouble(3, value);

			log.debug("Executing SQL update: {}", setPreferenceSQL);
			stmt.executeUpdate();

		} catch (SQLException sqle) {
			log.warn("Exception while setting preference", sqle);
			throw new TasteException(sqle);
		} finally {
			IOUtils.quietClose(null, stmt, conn);
		}
	}
}
