package org.wso2.carbon.social.sql;

public class Constants {
    public static final String BODY_COLUMN = "body";
    public static final String ID_COLUMN = "id";
    public static final String CONTEXT_ID_COLUMN = "payload_context_id";
    public static final String VOTE_COLUMN = "votes";

    public static final String TARGET_JSON_PROP = "target";
    public static final String CONTEXT_JSON_PROP = "context";
    public static final String ACTOR_JSON_PROP = "actor";
    public static final String ID_JSON_PROP = "id";
    public static final String OBJECT_JSON_PROP = "object";
    public static final String RATING_JSON_PROP = "rating";

    //SOCIAL DTABASE TABLE
    public static final String ES_SOCIAL_COMMENT_TBL = "ES_SOCIAL_COMMENT";
    public static final String UPDATE_VOTE_SQL = "UPDATE " + ES_SOCIAL_COMMENT_TBL + " SET " + VOTE_COLUMN + " = ? WHERE " + ID_COLUMN + "=?";
    public static final String SELECT_VOTE_SQL = "SELECT * FROM " + ES_SOCIAL_COMMENT_TBL + " WHERE " + ID_COLUMN + "=?";
    public static final String INSERT_COMMENT_SQL = "INSERT INTO " + ES_SOCIAL_COMMENT_TBL + "(id, payload_context_id, body, user_id, votes, rating, timestamp) VALUES(?, ?, ?, ?, ?, ?, ?)";
    public static final String ES_SOCIAL_RATE_TBL = "ES_SOCIAL_RATE";
    public static final String ES_SOCIAL_VOTE_TBL = "ES_SOCIAL_VOTE";

    public static final String DSETUP_PATTERN = ".*-Dsetup.*";
    public static final String SETUP_CMD = "setup";
    public static final String SOCIAL_DB_NAME = "WSO2_SOCIAL_DB";

}
