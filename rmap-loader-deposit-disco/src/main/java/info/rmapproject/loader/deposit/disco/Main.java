/*
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.rmapproject.loader.deposit.disco;

import static info.rmapproject.loader.util.ActiveMQConfig.buildConnectionFactory;
import static info.rmapproject.loader.util.ConfigUtil.integer;
import static info.rmapproject.loader.util.ConfigUtil.string;
import static info.rmapproject.loader.util.LogUtil.adjustLogLevels;

import java.net.URI;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

/**
 * @author apb@jhu.edu
 */
public class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    @SuppressWarnings("resource")
    public static void main(final String[] args) throws Exception {

        final ExecutorService exe = Executors.newCachedThreadPool();

        final CompletionService<AutoCloseable> tasks = new ExecutorCompletionService<>(exe);

        final int nthreads = integer("threads", 1);

        adjustLogLevels();
        final ConnectionFactory factory = buildConnectionFactory();

        final HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(string("jdbc.url", "jdbc:sqlite:"));
        ds.setUsername(string("jdbc.username", null));
        ds.setPassword(string("jdbc.password", null));

        final RdbmsHarvestRecordRegistry harvestRegistry = new RdbmsHarvestRecordRegistry();
        harvestRegistry.setDataSource(ds);
        harvestRegistry.init();

        final DiscoDepositConsumer depositor = new DiscoDepositConsumer();
        depositor.setAuthToken(string("rmap.api.auth.token", null));
        depositor.setRmapDiscoEndpoint(makeDiscoEndpointUri());
        depositor.setHarvestRegistry(harvestRegistry);

        for (int i = 0; i < nthreads; i++) {

            LOG.info("Starting deposit thread " + i);

            final DiscoDepositService depositService = new DiscoDepositService();
            depositService.setConnectionFactory(factory);
            depositService.setDiscoConsumer(depositor);
            depositService.setQueueSpec(string("jms.queue.src", "rmap.harvest.disco.>"));

            tasks.submit(depositService, depositService);
        }

        for (int i = 0; i < nthreads; i++) {
            tasks.take().get().close();

            LOG.info("Stopped deposit thread " + i);
        }
    }

    private static URI makeDiscoEndpointUri() {
        return URI.create(string("rmap.api.baseuri",
                "https://test.rmap-hub.org/api/").replaceFirst("/$", "") + "/discos/");
    }
}
