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
package org.neo4j.gds.embeddings.graphsage.ddl4j.functions;

import org.neo4j.gds.embeddings.graphsage.ddl4j.ComputationContext;
import org.neo4j.gds.embeddings.graphsage.ddl4j.Dimensions;
import org.neo4j.gds.embeddings.graphsage.ddl4j.Tensor;
import org.neo4j.gds.embeddings.graphsage.ddl4j.Variable;

public class ElementwiseMax extends SingleParentVariable {
    private final int[][] adjacencyMatrix;

    public ElementwiseMax(Variable parent, int[][] adjacencyMatrix) {
        super(parent, Dimensions.matrix(adjacencyMatrix.length, parent.dimension(1)));
        this.adjacencyMatrix = adjacencyMatrix;
    }

    @Override
    protected Tensor apply(ComputationContext ctx) {
        Tensor max = Tensor.constant(Double.MIN_VALUE, dimensions());

        int rows = dimension(0);
        int cols = dimension(1);
        double[] parentData = ctx.data(parent).data;
        for (int row = 0; row < rows; row++) {
            int[] neighbors = this.adjacencyMatrix[row];
            for(int dimension = 0; dimension < cols; dimension++) {
                if (neighbors.length > 0) {
                    for (int n: neighbors) {
                        max.data[row * cols + dimension] =
                            Math.max(parentData[n * cols + dimension], max.data[row * cols + dimension]);
                    }
                } else {
                    max.data[row * cols + dimension] = 0;
                }
            }
        }

        return max;
    }

    @Override
    protected Tensor gradient(ComputationContext ctx) {
        Tensor result = ctx.data(parent).zeros();

        int cols = parent.dimension(1);

        double[] parentData = ctx.data(parent).data;
        double[] thisGradient = ctx.gradient(this).data;
        double[] thisData = ctx.data(this).data;

        for (int adjacentRow = 0; adjacentRow < this.adjacencyMatrix.length; adjacentRow++) {
            int[] neighbors = this.adjacencyMatrix[adjacentRow];
            for (int dimension = 0; dimension < cols; dimension++) {
                for (int neighborIdx : neighbors) {
                    if (parentData[neighborIdx * cols + dimension] == thisData[adjacentRow * cols + dimension]) {
                        result.data[neighborIdx * cols + dimension] += thisGradient[adjacentRow * cols + dimension];
                    }
                }
            }
        }

        return result;
    }
}
