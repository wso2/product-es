package org.wso2.carbon.social.sql;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.SortOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.social.sql.Constants.*;

public class SQLActivityBrowser implements ActivityBrowser {
    private static final Log log = LogFactory.getLog(SQLActivityBrowser.class);
    private static final JsonParser parser = new JsonParser();

    @Override
    public JsonObject getSocialObject(String contextId, String tenantDomain, SortOrder order) {
        DSConnection con = new DSConnection(); //TODO: shouldn't new this,use singleton
        Connection connection = con.getConnection();
        String objectJson = "{}";
        if (connection != null) {
            PreparedStatement statement;
            ResultSet resultSet;
            try {
                statement = connection.prepareStatement(SELECT_OBJECT_QUERY);
                statement.setString(1, contextId);
                statement.setString(2, tenantDomain);
                resultSet = statement.executeQuery();
                resultSet.next();
                objectJson = resultSet.getString(1);
            } catch (SQLException e) {
                // TODO: throw it as a custom Exception type.
                log.error("Can't retrieve average rating from SQL.", e);
            } finally {
                con.closeConnection(connection);
            }
        }
        return (JsonObject) parser.parse(objectJson);
    }

    @Override
    public List<Activity> listActivities(String contextId, String tenantDomain) {
        return loadActivitiesFromDB(contextId, tenantDomain, SELECT_ACTIVITIES_QUERY);
    }

    @Override
    public List<Activity> listActivitiesChronologically(String contextId, String tenantDomain) {
        return loadActivitiesFromDB(contextId, tenantDomain, SELECT_ORDERED_ACTIVITIES_QUERY);
    }

    @Override
    public double getRating(String contextId, String tenantDomain) {
        DSConnection con = new DSConnection();//TODO: shouldn't new this,use singleton
        Connection connection = con.getConnection();
        double averageRating = 0.0;
        if (connection != null) {
            PreparedStatement statement;
            ResultSet resultSet;
            try {
                statement = connection.prepareStatement(SELECT_RATING_QUERY);
                statement.setString(1, contextId);
                statement.setString(2, tenantDomain);
                resultSet = statement.executeQuery();
                resultSet.next();
                averageRating = Double.parseDouble(resultSet.getString(1));
            } catch (SQLException e) {
                // TODO: throw it as a custom Exception type.
                log.error("Can't retrieve average rating from SQL.", e);
            } finally {
                con.closeConnection(connection);
            }
        }
        return averageRating;
    }

    private List<Activity> loadActivitiesFromDB(String contextId, String tenantDomain, String query) {
        List<Activity> activities = null;
        DSConnection con = new DSConnection(); //TODO: shouldn't new this,use singleton
        Connection connection = con.getConnection();
        if (connection != null) {
            PreparedStatement statement;
            ResultSet resultSet;
            try {
                statement = connection.prepareStatement(query);
                statement.setString(1, contextId);
                statement.setString(2, tenantDomain);
                resultSet = statement.executeQuery();
                activities = new ArrayList<Activity>();
                while (resultSet.next()) {
                    SQLActivity activity = new SQLActivity(resultSet.getString(BODY_COLUMN),
                            resultSet.getTimestamp(TIMESTAMP_COLUMN).getTime());
                    activities.add(activity);
                }
            } catch (SQLException e) {
                // TODO: throw it
                log.error("Can't retrieve activities from SQL.", e);
            } finally {
                con.closeConnection(connection);
            }
        }
        return activities;
    }
}
