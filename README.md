# Currency-Arbitrage-Loop-Algorithm
This project aims to find currency arbitrage loops to perform a profitable trading order. 

For example, using the below table and starting with $100 US,

| FROM / TO | USD | EUR | JPY | BTC |
| - | - | - | - | - |
| **USD** | - | 0.7779 | 102.4590 | 0.0083 | 
| **EUR** | 1.2851 | - | 131.7110 | 0.01125 | 
| **JPY** | 0.0098 | 0.0075 | - | 0.0000811 | 
| **BTC** | 115.65 | 88.8499 | 12325.44 | - | 

We can:
- Trade $100 to €77.79
- Trade €77.79 to .8751375 BTC
- Trade .8751375 BTC for $101.20965

## How to run

Run from the IDE with a run configuration, for example, "ArbitrageOpportunities USD" where USD is a valid coin.


## About the solution

In this problem, the data forms a fully connected graph: all vertices are connected to each other.
It is a dense graph meaning the number of edges is approximately the number of vertices squared: |E|=|V*V-V|~=|V|^2.
The chosen implementation for this problem uses an adjacency matrix because it is a dense graph, using |V|^2 space to store the data. The other option could be the adjacency list, that is good for sparse graphs, using less space in that case.
Solutions to this problem are graph paths with a factor that is all the weights of all the traversed edges multiplied by each other.
The optimal solutions are the ones with the biggest factors (it is possible to have more than one optimal solution, if this is the case we can choose the solution with the shortest path). If we multiply this factor by some amount of the input coin, we have the returning yield for doing the transactions in the path.

## Algorithmic complexity analysis
 
The time and space complexity are O(E=V*V-V~=V^2), because it is a brute force algorithm: we have to find all possible paths in order to select only the ones that end in our starting vertex and calculate the best of those to make the transactions that provide the best yield.

