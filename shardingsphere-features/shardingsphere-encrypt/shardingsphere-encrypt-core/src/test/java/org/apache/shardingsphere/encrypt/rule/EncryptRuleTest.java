/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.encrypt.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.fixture.CustomizedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.fixture.TestEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class EncryptRuleTest {
    
    @Test
    public void assertNewInstanceWithAlgorithmProvidedEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration("encrypt_column", "encrypt_cipher", "", "", "test_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singletonList(encryptColumnConfig), null);
        AlgorithmProvidedEncryptRuleConfiguration ruleConfig = new AlgorithmProvidedEncryptRuleConfiguration(
                Collections.singleton(tableConfig), ImmutableMap.of("test_encryptor", new TestEncryptAlgorithm()), true);
        EncryptRule actual = new EncryptRule(ruleConfig);
        assertTrue(actual.findEncryptTable("t_encrypt").isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewInstanceWithInvalidConfiguration() {
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("TEST", new Properties());
        EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration("encrypt_column", "encrypt_cipher", "", "", "test_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singletonList(encryptColumnConfig), null);
        EncryptRuleConfiguration ruleConfig = new EncryptRuleConfiguration(Collections.singleton(tableConfig), ImmutableMap.of("invalid_encryptor", encryptAlgorithmConfig));
        new EncryptRule(ruleConfig);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewInstanceWithInvalidAlgorithmProvidedEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration("encrypt_column", "encrypt_cipher", "", "", "test_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singletonList(encryptColumnConfig), null);
        AlgorithmProvidedEncryptRuleConfiguration ruleConfig = new AlgorithmProvidedEncryptRuleConfiguration(
                Collections.singleton(tableConfig), ImmutableMap.of("invalid_encryptor", new TestEncryptAlgorithm()), true);
        new EncryptRule(ruleConfig);
    }
    
    @Test
    public void assertFindEncryptTable() {
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).findEncryptTable("t_encrypt").isPresent());
    }
    
    @Test
    public void assertFindEncryptor() {
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).findEncryptor(DefaultSchema.LOGIC_NAME, "t_encrypt", "pwd").isPresent());
    }
    
    @Test
    public void assertNotFindEncryptor() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).findEncryptor(DefaultSchema.LOGIC_NAME, "t_encrypt", "other_column").isPresent());
    }
    
    @Test
    public void assertGetEncryptValues() {
        List<Object> encryptAssistedQueryValues = new EncryptRule(createEncryptRuleConfiguration()).getEncryptValues(DefaultSchema.LOGIC_NAME, "t_encrypt", "pwd", 
                Collections.singletonList(null));
        for (final Object value : encryptAssistedQueryValues) {
            assertNull(value);
        }
    }
    
    @Test
    public void assertGetCipherColumnWhenEncryptColumnExist() {
        assertThat(new EncryptRule(createEncryptRuleConfiguration()).getCipherColumn("t_encrypt", "pwd"), is("pwd_cipher"));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetCipherColumnWhenNoEncryptColumn() {
        new EncryptRule(createEncryptRuleConfiguration()).getCipherColumn("t_encrypt", "pwd_cipher");
    }
    
    @Test
    public void assertGetLogicAndCipherColumns() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).getLogicAndCipherColumns("t_encrypt").isEmpty());
    }
    
    @Test
    public void assertFindAssistedQueryColumn() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).findAssistedQueryColumn("t_encrypt", "pwd_cipher").isPresent());
    }
    
    @Test
    public void assertGetEncryptAssistedQueryValues() {
        List<Object> encryptAssistedQueryValues = new EncryptRule(createEncryptRuleConfiguration()).getEncryptAssistedQueryValues(DefaultSchema.LOGIC_NAME, "t_encrypt", "pwd", 
                Collections.singletonList(null));
        for (final Object value : encryptAssistedQueryValues) {
            assertNull(value);
        }
    }
    
    @Test
    public void assertGetAssistedQueryColumns() {
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).getAssistedQueryColumns("t_encrypt").isEmpty());
    }
    
    @Test
    public void assertFindPlainColumn() {
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).findPlainColumn("t_encrypt", "pwd").isPresent());
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).findPlainColumn("t_encrypt", "credit_card".toLowerCase()).isPresent());
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).findPlainColumn("t_encrypt", "notExistLogicColumn").isPresent());
    }
    
    @Test
    public void assertGetTables() {
        assertThat(new EncryptRule(createEncryptRuleConfiguration()).getTables(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    public void assertGetRuleType() {
        assertThat(new EncryptRule(createEncryptRuleConfiguration()).getType(), is(EncryptRule.class.getSimpleName()));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertSetUpEncryptorSchema() {
        EncryptRule encryptRule = new EncryptRule(createEncryptRuleConfiguration());
        encryptRule.setUpEncryptorSchema(mock(ShardingSphereSchema.class));
        Optional<EncryptAlgorithm> actual = encryptRule.findEncryptor("t_encrypt", "name");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(CustomizedEncryptAlgorithm.class));
        assertNotNull(((CustomizedEncryptAlgorithm) actual.get()).getSchema());
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        ShardingSphereAlgorithmConfiguration queryAssistedEncryptor = new ShardingSphereAlgorithmConfiguration("QUERY_ASSISTED_TEST", new Properties());
        ShardingSphereAlgorithmConfiguration customizedEncryptor = new ShardingSphereAlgorithmConfiguration("CUSTOMIZED", new Properties());
        EncryptColumnRuleConfiguration pwdColumnConfig = new EncryptColumnRuleConfiguration("pwd", "pwd_cipher", "", "pwd_plain", "test_encryptor");
        EncryptColumnRuleConfiguration creditCardColumnConfig = new EncryptColumnRuleConfiguration("credit_card", "credit_card_cipher", "", "credit_card_plain", "test_encryptor");
        EncryptColumnRuleConfiguration nameColumnConfig = new EncryptColumnRuleConfiguration("name", "name_cipher", "", "name_plain", "customized_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Arrays.asList(pwdColumnConfig, creditCardColumnConfig, nameColumnConfig), null);
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), ImmutableMap.of("test_encryptor", queryAssistedEncryptor, "customized_encryptor", customizedEncryptor));
    }
}
