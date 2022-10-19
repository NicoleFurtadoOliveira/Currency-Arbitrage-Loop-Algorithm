import scala.collection.mutable
import scala.io.Source.fromURL
import scala.util.{Failure, Success, Try}

class Vertex(val name: String, val number: Int)

class Edge(val from: Vertex, val to: Vertex, val weight: Float) {
  override def toString: String = "(" + from.number + ", " + to.number + ", " + weight + ")"

  private var visited: Boolean = false

  def isVisited: Boolean = visited

  def visit(): Unit = visited = true

  def unVisit(): Unit = visited = false
}

class Path(val edges: Vector[Edge]) {
  def append(edge: Edge) = new Path(edges :+ edge)

  override def toString: String = edges.mkString(", ")
}

class FullyConnectedGraph(val matrix: Array[Array[Edge]]) {

  val numberVertices: Int = matrix.length

  def printGraph(): Unit = {
    println("Graph:")
    for (i <- 0 until numberVertices) {
      for (j <- 0 until numberVertices) {
        if (i == 0 && j == 0) print("[")
        val comma = if (i != numberVertices - 1 || j != numberVertices - 1) ",  " else ""
        val lastString = if (i == numberVertices - 1 && j == numberVertices - 1) "]" else ""
        print(matrix(i)(j) + comma + lastString)
      }
      println()
    }
  }

  def getAdjacentVertices(u: Edge): Array[Edge] = {
    matrix(u.to.number).filterNot(_.equals(matrix(u.to.number)(u.to.number)))
  }

  def getAllPathsFrom(startVertex: Int): mutable.Set[Path] = {

    def getAllPathsFrom(edge: Edge, currentPath: Path, paths: mutable.Set[Path]): mutable.Set[Path] = {
      paths += currentPath
      edge.visit()
      for (i <- getAdjacentVertices(edge)) {
        if (!i.isVisited) {
          getAllPathsFrom(i, currentPath.append(i), paths)
        }
      }
      edge.unVisit()
      paths
    }

    val startEdge = matrix(startVertex)(startVertex)
    val paths: mutable.Set[Path] = mutable.Set(new Path(Vector.empty))
    getAllPathsFrom(startEdge, new Path(Vector.empty), paths)
    paths
  }

}

class Solution(val path: Path, val factor: Float) {
  override def toString: String = "(" + path + ", " + factor + ")"

  def toStringWithLabels: String = "(" + path.edges.map(edge => (edge.from.name, edge.to.name, edge.weight).toString()).mkString(", ") + ", " + factor.toString + ")"
}

object ArbitrageOpportunities {

  private def getEdgesSetNumberOfVerticesAndStartVertex(data: String, startCoin: String): (mutable.Set[Edge], Int, Int) = {
    val vertexNameToNumberMap = mutable.Map[String, Int]()
    var currVertexNumber = 0
    val edges: mutable.Set[Edge] = mutable.Set()
    //Data to Seq("JPY-JPY: 1.0000000", "USD-EUR: 0.7644769") format
    val edgesRaw = data.replaceAll("[ \n{}\"]", "").split(",").toSeq
    edgesRaw.map { edgeRaw =>
      //Data to Array("PY-JPY", "1.0000000") format
      val edgeRawSplit = edgeRaw.split(":")
      //Data to Array("PY","JPY") format
      val verticesRaw = edgeRawSplit(0).split("-")

      def processVertexNumberFromName(vertexName: String): Int = {
        if (vertexNameToNumberMap.contains(vertexName)) vertexNameToNumberMap(vertexName) else {
          vertexNameToNumberMap += (vertexName -> currVertexNumber)
          currVertexNumber += 1
          currVertexNumber - 1
        }
      }

      val vertexFromString = verticesRaw(0)
      val vertexFrom = new Vertex(vertexFromString, processVertexNumberFromName(vertexFromString))
      val vertexToString = verticesRaw(1)
      val vertexTo = new Vertex(vertexToString, processVertexNumberFromName(vertexToString))
      edges += new Edge(vertexFrom, vertexTo, edgeRawSplit(1).toFloat)
    }
    val availableCoins = vertexNameToNumberMap.keySet
    if (!availableCoins.contains(startCoin))
      throw new Exception("The starting coin " + startCoin + " is not present in the rates data. A starting coin must be provided from the available starting coins: " + availableCoins.mkString(", "))

    (edges, currVertexNumber, vertexNameToNumberMap(startCoin))
  }

  def main(args: Array[String]): Unit = {
    val debug = true

    var startCoin = ""
    Try(args(0)) match {
      case Failure(_) => throw new Exception("A valid starting coin must be provided.")
      case Success(value) => startCoin = value
    }

    //Static data
    val data = "{\n  \"JPY-JPY\": \"1.0000000\",\n  \"USD-EUR\": \"0.7644769\",\n  \"BTC-EUR\": \"101.1658755\",\n  \"USD-BTC\": \"0.0081586\",\n  \"EUR-BTC\": \"0.0096866\",\n  \"EUR-USD\": \"1.1063850\",\n  \"EUR-EUR\": \"1.0000000\",\n  \"JPY-BTC\": \"0.0000824\",\n  \"USD-USD\": \"1.0000000\",\n  \"BTC-BTC\": \"1.0000000\",\n  \"USD-JPY\": \"100.6910144\",\n  \"JPY-EUR\": \"0.0077178\",\n  \"JPY-USD\": \"0.0099213\",\n  \"BTC-USD\": \"137.1507501\",\n  \"EUR-JPY\": \"113.3580399\",\n  \"BTC-JPY\": \"14039.6031102\"\n}"

    //Process the data into a set of Edges and get the number of vertexes
    val (edges, numberOfVertices, startVertex) = getEdgesSetNumberOfVerticesAndStartVertex(data, startCoin)

    //Initialize adjacency matrix, each entry is an edge with source, destination and weight
    val matrix = Array.ofDim[Edge](numberOfVertices, numberOfVertices)
    edges.foreach { edge =>
      val i = edge.from.number
      val j = edge.to.number
      matrix(i)(j) = edge
    }

    //Create graph and print it
    val graph = new FullyConnectedGraph(matrix)
    if (debug) {
      graph.printGraph()
    }
    val paths = graph.getAllPathsFrom(startVertex)
    //Filter by the non empty paths that end on our start vertex
    val filteredPaths = paths.filter(path => path.edges.nonEmpty).filter(path => path.edges.last.to.number == startVertex)
    //Create the solutions that are a path with a factor that is the product of all edges' weight
    val solutions = filteredPaths.map(path => new Solution(path, path.edges.map(_.weight).product))
    val orderedSolutionsByFactor = solutions.toSeq.sortBy(_.factor)
    if (debug) {
      println("Ordered arbitrage opportunities:")
      orderedSolutionsByFactor.foreach(println(_))
    }
    val solutionWithBiggestFactor = orderedSolutionsByFactor.last
    val bestSolutions = orderedSolutionsByFactor.filter(_.factor == solutionWithBiggestFactor.factor)
    if (debug) {
      println("Best arbitrage opportunities by coin id:")
      bestSolutions.foreach(println(_))
    }
    println("Best arbitrage opportunities:")
    bestSolutions.foreach(solution => println(solution.toStringWithLabels))
  }
}