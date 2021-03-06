/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.beta.fastrp;

import org.neo4j.gds.embeddings.fastrp.FastRP;
import org.neo4j.graphalgo.AlgorithmFactory;
import org.neo4j.graphalgo.StatsProc;
import org.neo4j.graphalgo.config.GraphCreateConfig;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphalgo.result.AbstractResultBuilder;
import org.neo4j.graphalgo.results.MemoryEstimateResult;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.neo4j.graphalgo.beta.fastrp.FastRPExtendedCompanion.DESCRIPTION;
import static org.neo4j.procedure.Mode.READ;

public class FastRPExtendedStatsProc extends StatsProc<FastRP, FastRP, FastRPExtendedStatsProc.StatsResult, FastRPExtendedStatsConfig> {

    @Procedure(value = "gds.beta.fastRPExtended.stats", mode = READ)
    @Description(DESCRIPTION)
    public Stream<StatsResult> stats(
        @Name(value = "graphName") Object graphNameOrConfig,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        ComputationResult<FastRP, FastRP, FastRPExtendedStatsConfig> computationResult = compute(
            graphNameOrConfig,
            configuration
        );
        return stats(computationResult);
    }
    @Procedure(value = "gds.beta.fastRPExtended.stats.estimate", mode = READ)
    @Description(DESCRIPTION)
    public Stream<MemoryEstimateResult> estimate(
        @Name(value = "graphName") Object graphNameOrConfig,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        return computeEstimate(graphNameOrConfig, configuration);
    }


    @Override
    protected AbstractResultBuilder<StatsResult> resultBuilder(ComputationResult<FastRP, FastRP, FastRPExtendedStatsConfig> computeResult) {
        return new StatsResult.Builder();
    }

    @Override
    protected FastRPExtendedStatsConfig newConfig(
        String username,
        Optional<String> graphName,
        Optional<GraphCreateConfig> maybeImplicitCreate,
        CypherMapWrapper config
    ) {
        return FastRPExtendedStatsConfig.of(username, graphName, maybeImplicitCreate, config);
    }

    @Override
    protected AlgorithmFactory<FastRP, FastRPExtendedStatsConfig> algorithmFactory() {
        return new FastRPExtendedFactory<>();
    }

    public static final class StatsResult {

        public final long nodeCount;
        public final long createMillis;
        public final long computeMillis;
        public final Map<String, Object> configuration;

        StatsResult(
            long nodeCount,
            long createMillis,
            long computeMillis,
            Map<String, Object> config
        ) {
            this.nodeCount = nodeCount;
            this.createMillis = createMillis;
            this.computeMillis = computeMillis;
            this.configuration = config;
        }

        static final class Builder extends AbstractResultBuilder<StatsResult> {

            @Override
            public StatsResult build() {
                return new StatsResult(
                    nodeCount,
                    createMillis,
                    computeMillis,
                    config.toMap()
                );
            }
        }
    }
}
