/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;

/**
 * Base tests for DbUserMasterWorker via DbUserMaster.
 */
public abstract class AbstractDbUserMasterWorkerTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbUserMasterWorkerTest.class);

  protected DbUserMaster _usrMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalUsers;
  protected boolean _readOnly;  // attempt to speed up tests

  public AbstractDbUserMasterWorkerTest(String databaseType, String databaseVersion, boolean readOnly) {
    super(databaseType, databaseVersion, databaseVersion);
    _readOnly = readOnly;
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeClass
  public void setUpClass() throws Exception {
    if (_readOnly) {
      init();
    }
  }

  @BeforeMethod
  public void setUp() throws Exception {
    if (_readOnly == false) {
      init();
    }
  }

  protected ObjectId setupTestData(Instant now) {
    TimeSource origTimeSource = _usrMaster.getTimeSource();
    try {
      _usrMaster.setTimeSource(TimeSource.fixed(now));

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      ManageableOGUser user = new ManageableOGUser("initial");
      user.setExternalIdBundle(bundle);
      UserDocument initialDoc = new UserDocument(user);

      _usrMaster.add(initialDoc);

      ObjectId baseOid = initialDoc.getObjectId();

      //------------------------------------------------------------------------------------------------------------------
      List<UserDocument> firstReplacement = newArrayList();
      for (int i = 0; i < 5; i++) {
        ManageableOGUser ex = new ManageableOGUser("setup_" + i);
        UserDocument doc = new UserDocument(ex);
        doc.setVersionFromInstant(now.plus(i, TimeUnit.MINUTES));
        firstReplacement.add(doc);
      }
      _usrMaster.setTimeSource(TimeSource.fixed(now.plus(1, TimeUnit.HOURS)));
      _usrMaster.replaceVersions(baseOid, firstReplacement);

      return baseOid;
    } finally {
      _usrMaster.setTimeSource(origTimeSource);
    }
  }

  private void init() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _usrMaster = (DbUserMaster) context.getBean(getDatabaseType() + "DbUserMaster");
    
//    id bigint NOT NULL,
//    oid bigint NOT NULL,
//    ver_from_instant timestamp without time zone NOT NULL,
//    ver_to_instant timestamp without time zone NOT NULL,
//    corr_from_instant timestamp without time zone NOT NULL,
//    corr_to_instant timestamp without time zone NOT NULL,
//    userid varchar(255) NOT NULL,
//    password varchar(255) NOT NULL,
//    name varchar(255),
//    time_zone varchar(255),
//    email_address varchar(255),
    Instant now = Instant.now();
    _usrMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _usrMaster.getDbConnector().getJdbcTemplate();
    ManageableOGUser user = new ManageableOGUser("101");
    user.setUniqueId(UniqueId.of("DbUsr", "101", "0"));
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    user.setUserId("Test101");
    user.setName("TestUser101");
    user.setTimeZone(TimeZone.of("Europe/London"));
    template.update("INSERT INTO usr_oguser VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "Test101", "PW", "TestUser101", "Europe/London", "email101@email.com");
    user.setUniqueId(UniqueId.of("DbUsr", "102", "0"));
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H")));
    user.setUserId("Test102");
    user.setName("TestUser102");
    user.setTimeZone(TimeZone.of("Europe/Paris"));
    template.update("INSERT INTO usr_oguser VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "Test102", "PW", "TestUser102", "Europe/Paris", "email102@email.com");
    user.setUniqueId(UniqueId.of("DbUsr", "201", "0"));
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    user.setUserId("Test201");
    user.setName("TestUser201");
    user.setTimeZone(TimeZone.of("Asia/Tokyo"));
    template.update("INSERT INTO usr_oguser VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "Test201", "PW", "TestUser201", "Asia/Tokyo", "email201@email.com");
    user.setUniqueId(UniqueId.of("DbUsr", "201", "1"));
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    user.setUserId("Test202");
    user.setName("TestUser202");
    user.setTimeZone(TimeZone.of("Asia/Tokyo"));
    template.update("INSERT INTO usr_oguser VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP,
        "Test202", "PW", "TestUser202", "Asia/Tokyo", "email202@email.com");
    _totalUsers = 3;
//  id bigint not null,
//  key_scheme varchar(255) not null,
//  key_value varchar(255) not null,
    template.update("INSERT INTO usr_idkey VALUES (?,?,?)",
        1, "A", "B");
    template.update("INSERT INTO usr_idkey VALUES (?,?,?)",
        2, "C", "D");
    template.update("INSERT INTO usr_idkey VALUES (?,?,?)",
        3, "E", "F");
    template.update("INSERT INTO usr_idkey VALUES (?,?,?)",
        4, "G", "H");
//  user_id bigint not null,
//  idkey_id bigint not null,
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        101, 1);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        101, 2);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        101, 3);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        102, 1);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        102, 2);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        102, 4);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        201, 2);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        201, 3);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        202, 2);
    template.update("INSERT INTO usr_oguser2idkey VALUES (?,?)",
        202, 3);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_readOnly == false) {
      _usrMaster = null;
      super.tearDown();
    }
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    if (_readOnly) {
      _usrMaster = null;
      super.tearDown();
    }
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final UserDocument test) {
    UniqueId uniqueId = UniqueId.of("DbUsr", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOGUser user = test.getUser();
    assertNotNull(user);
    assertEquals(uniqueId, user.getUniqueId());
    assertEquals("Test101", test.getUser().getUserId());
    assertEquals("TestUser101", test.getName());
    assertEquals(TimeZone.of("Europe/London"), user.getTimeZone());
    assertEquals("email101@email.com", user.getEmailAddress());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")), user.getExternalIdBundle());
  }

  protected void assert102(final UserDocument test) {
    UniqueId uniqueId = UniqueId.of("DbUsr", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOGUser user = test.getUser();
    assertNotNull(user);
    assertEquals(uniqueId, user.getUniqueId());
    assertEquals("Test102", test.getUser().getUserId());
    assertEquals("TestUser102", test.getName());
    assertEquals(TimeZone.of("Europe/Paris"), user.getTimeZone());
    assertEquals("email102@email.com", user.getEmailAddress());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H")), user.getExternalIdBundle());
  }

  protected void assert201(final UserDocument test) {
    UniqueId uniqueId = UniqueId.of("DbUsr", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOGUser user = test.getUser();
    assertNotNull(user);
    assertEquals(uniqueId, user.getUniqueId());
    assertEquals("Test201", test.getUser().getUserId());
    assertEquals("TestUser201", test.getName());
    assertEquals(TimeZone.of("Asia/Tokyo"), user.getTimeZone());
    assertEquals("email201@email.com", user.getEmailAddress());
    assertEquals(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")), user.getExternalIdBundle());
  }

  protected void assert202(final UserDocument test) {
    UniqueId uniqueId = UniqueId.of("DbUsr", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOGUser user = test.getUser();
    assertNotNull(user);
    assertEquals(uniqueId, user.getUniqueId());
    assertEquals("Test202", test.getUser().getUserId());
    assertEquals("TestUser202", test.getName());
    assertEquals(TimeZone.of("Asia/Tokyo"), user.getTimeZone());
    assertEquals("email202@email.com", user.getEmailAddress());
    assertEquals(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")), user.getExternalIdBundle());
  }

}
