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

package org.apache.shardingsphere.data.pipeline.core.datasource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreatorFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Pipeline data source factory.
 */
public final class PipelineDataSourceFactory {
    
    /**
     * New instance data source wrapper.
     *
     * @param dataSourceConfig data source configuration
     * @return new data source wrapper
     */
    @SneakyThrows(SQLException.class)
    public PipelineDataSourceWrapper newInstance(final PipelineDataSourceConfiguration dataSourceConfig) {
        // TODO cache and reuse, try PipelineDataSourceManager
        DataSource pipelineDataSource = PipelineDataSourceCreatorFactory.getInstance(dataSourceConfig.getType()).createPipelineDataSource(dataSourceConfig.getDataSourceConfiguration());
        return new PipelineDataSourceWrapper(pipelineDataSource, dataSourceConfig.getDatabaseType());
    }
}
