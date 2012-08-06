package axle.stats

/**
 * Notes on unimplemented aspects of Bayesian networks:
 *
 * Bayes Rule
 *
 * pr(A=a|B=b) = pr(B=b|A=a)pr(A=a)/pr(B=b)
 *
 * Case analysis
 *
 * pr(A=a) = pr(A=a, B=b1) + ... + pr(A=a, B=bN)
 *
 * Chain Rule
 *
 * pr(X1=x1, ..., XN = xN) = pr(X1=x1 | X2=x2, ..., XN=xN) * ... * pr(XN=xN)
 *
 * Jeffrey's Rule
 *
 * ???
 *
 * markov independence: a variable is independent of its non-descendents given its parents
 *
 * d-separation cases:
 *
 * x: given variable (it appears in Z)
 * o: not given
 *
 * Sequence:
 *
 * -> o ->   open
 * -> x ->   closed
 *
 * Divergent:
 *
 * <- o ->   open
 * <- x ->   closed
 *
 * Convergent:
 *
 * -> o <-   closed (and none of the descendants of the node are in Z)
 * -> x <-   open (or if any of the descendants of the node are in Z)
 *
 *
 * A path is blocked (independent) if any valve in the path is blocked.
 *
 * Two variables are separated if all paths are blocked.
 *
 * Independence
 *
 * I(X, Z, Y) is read "X is independent of Y given Z"
 *
 * Symmetry (ch 3 stuff)
 *
 * I(X, Z, Y) <=> I(Y, Z, X)
 *
 * Decomposition
 *
 * I(X, Z, Y or W) <=> I(X, Z, Y) and I(X, Z, W)
 *
 * Weak Union
 *
 * I(X, Z, Y or W) => I(X, Z or Y, W)
 *
 * Contraction
 *
 * I(X, Z, Y) and I(X, Z or Y, W) => I(X, Z, Y or W)
 *
 * Intersection
 *
 * I(X, Z or W, Y) and I(X, Z or Y, W) => I(X, Z, Y or W)
 *
 * independence closure
 *
 * ???  used for what ???
 *
 * MPE: most probable explanation
 *
 * find {x1, ..., xN} such that pr(X1=x1, ..., XN=xN | e) is maximized
 *
 * MPE is much easier to compute algorithmically
 *
 * MAP: maximum a posteriori hypothesis
 *
 * Let M be a subset of network variables X
 *
 * Let e be some evidence
 *
 * Find an instantiation m over variables M such that pr(M=m|e) is maximized
 *
 * Tree networks (ch5 p20)
 *
 * are the ones where each node has at most one parent
 * treewidth <= 1
 *
 * A polytree is:
 *
 * there is at most one (undirected) path between any two nodes
 * treewidth = maximum number of parents
 *
 */

import collection._
import math.max
import axle.graph.JungDirectedGraphFactory._

class BayesianNetwork(name: String = "bn", g: JungDirectedGraph[RandomVariable[_], String]) extends Model(name, g) {

  def duplicate(): BayesianNetwork = new BayesianNetwork(name, graphFrom(g)(v => v, e => e))

  val var2cpt = mutable.Map[RandomVariable[_], Factor]()

  def getJointProbabilityTable(): Factor = {
    val jpt = new Factor(getRandomVariables())
    for (j <- 0 until jpt.numCases) {
      val c = jpt.caseOf(j)
      jpt.write(c, probabilityOf(c))
    }
    jpt
  }

  def setCPT(rv: RandomVariable[_], factor: Factor): Unit = var2cpt += rv -> factor

  def makeFactorFor(rv: RandomVariable[_]): Factor = {
    val preds = g.getPredecessors(g.findVertex(rv).get).map(_.getPayload)
    val cptVarList = getRandomVariables.filter(preds.contains(_))
    new Factor(cptVarList ++ List(rv), "cpt for " + rv.getName)
  }

  def getCPT(variable: RandomVariable[_]): Factor = {
    if (!var2cpt.contains(variable)) {
      var2cpt += variable -> makeFactorFor(variable)
    }
    var2cpt(variable)
  }

  def getAllCPTs(): List[Factor] = getRandomVariables.map(getCPT(_))

  def probabilityOf(c: CaseX) = c.getVariables.map(getCPT(_).read(c)).foldLeft(1.0)(_ * _)

  def getMarkovAssumptionsFor(rv: RandomVariable[_]): Independence = {

    val rvVertex = g.findVertex(rv).get

    val X: immutable.Set[RandomVariable[_]] = immutable.Set(rv)

    val Z: immutable.Set[RandomVariable[_]] = g.getPredecessors(rvVertex).map(_.getPayload).toSet

    val D = mutable.Set[g.V]()
    g.collectDescendants(rvVertex, D)
    D += rvVertex // probably already includes this
    D ++= g.getPredecessors(rvVertex)
    val Dvars = D.map(_.getPayload)
    val Y = getRandomVariables.filter(!Dvars.contains(_)).toSet

    new Independence(X, Z, Y)
  }

  def printAllMarkovAssumptions(): Unit =
    getRandomVariables.map(rv => println(getMarkovAssumptionsFor(rv)))

  def computeFullCase(c: CaseX): Double = {

    // not an airtight check
    assert(numVariables == c.size)

    // order variables such that all nodes appear before their ancestors
    // rewrite using chain rule
    // drop on conditionals in expressions using Markov independence assumptions
    // now each term should simply be a lookup in the curresponding CPT
    // multiply results

    -1.0 // TODO
  }

  def variableEliminationPriorMarginalI(Q: Set[RandomVariable[_]], π: List[RandomVariable[_]]): Factor = {
    // Algorithm 1 from Chapter 6 (page  9)

    // Q is a set of variables
    // pi is an ordered list of the variables not in Q
    // returns the prior marginal pr(Q)

    val S = π.foldLeft(getRandomVariables().map(getCPT(_)).toSet)((S, rv) => {
      val allMentions = S.filter(_.mentions(rv))
      val newS = S -- allMentions
      val T = Factor.multiply(allMentions.toList)
      val Ti = T.sumOut(rv)
      newS + Ti
    })
    Factor.multiply(S.toList)

    // the cost is the cost of the Tk multiplication
    // this is highly dependent on π
  }

  def variableEliminationPriorMarginalII(Q: Set[RandomVariable[_]], π: List[RandomVariable[_]], e: CaseX): Factor = {

    // Chapter 6 Algorithm 5 (page 17)

    // assert: Q subset of variables
    // assert: π ordering of variables in S but not in Q
    // assert: e assigns values to variables in this network

    val S = π.foldLeft(getRandomVariables().map(getCPT(_).projectRowsConsistentWith(Some(e))).toSet)(
      (S, rv) => {
        val allMentions = S.filter(_.mentions(rv))
        val newS = S -- allMentions
        val T = Factor.multiply(allMentions.toList)
        val Ti = T.sumOut(rv)
        newS + Ti
      })

    Factor.multiply(S.toList)
  }

  def interactsWith(v1: RandomVariable[_], v2: RandomVariable[_]): Boolean =
    getAllCPTs().exists(f => f.mentions(v1) && f.mentions(v2))

  // Also called the "moral graph"
  def interactionGraph(): InteractionGraph = {
    import axle.graph.JungUndirectedGraphFactory._
    val ig = graph[RandomVariable[_], String]()
    getRandomVariables.map(ig += _)
    val rvs = getRandomVariables()
    for (i <- 0 until rvs.size - 1) {
      val vi = rvs(i)
      val viVertex = ig.findVertex(vi).get
      for (j <- (i + 1) until rvs.size) {
        val vj = rvs(j)
        val vjVertex = ig.findVertex(vj).get
        if (interactsWith(vi, vj)) {
          ig.edge(viVertex, vjVertex, "")
        }
      }
    }
    new InteractionGraph(ig)
  }

  // Chapter 6 Algorithm 2 (page 13)
  def orderWidth(order: List[RandomVariable[_]]): Int =
    getRandomVariables().scanLeft((interactionGraph(), 0))(
      (gi, rv) => {
        val IG = gi._1
        val rvVertex = IG.getGraph.findVertex(rv).get
        val size = IG.getGraph.getNeighbors(rvVertex).size
        val newIG = IG.eliminate(rv)
        (newG, size)
      }
    ).map(_._2).max

  // 6.8.2
  def pruneEdges(resultName: String, eOpt: Option[CaseX]): BayesianNetwork = {
    import axle.graph.JungDirectedGraphFactory._
    val outG = graphFrom(g)(v => v, e => e)
    val result = new BayesianNetwork(resultName, outG)
    eOpt.map(e => {
      for (U <- e.getVariables()) {
        val uVertex = outG.findVertex(U).get
        for (edge <- outG.outputEdgesOf(uVertex)) { // ModelEdge
          val X = edge.getDest().getPayload
          val oldF = result.getCPT(X)
          outG.deleteEdge(edge) // TODO: this should be acting on a copy
          val smallerF = makeFactorFor(X)
          for (i <- 0 until smallerF.numCases) {
            val c = smallerF.caseOf(i)
            // set its value to what e sets it to
            c.assign(U, e.valueOf(U))
            val oldValue = oldF.read(c)
            smallerF.write(smallerF.caseOf(i), oldValue)
          }
          result.setCPT(edge.getDest().getPayload, smallerF) // TODO should be setting on the return value
        }
      }
      result
    }).getOrElse(result)
  }

  def pruneNodes(Q: Set[RandomVariable[_]], eOpt: Option[CaseX], g: JungDirectedGraph[RandomVariable[_], String]): JungDirectedGraph[RandomVariable[_], String] = {

    val vars = eOpt.map(Q ++ _.getVariables).getOrElse(Q)

    def nodePruneStream(g: JungDirectedGraph[RandomVariable[_], String]): Stream[JungDirectedGraph[RandomVariable[_], String]] = {
      val X = g.getLeaves().toSet -- vars // TODO: hidden type mismatch
      X.size match {
        case 0 => Stream.empty
        case _ => {
          val newG = g -- X
          Stream.cons(newG, nodePruneStream(newG))
        }
      }
    }
    nodePruneStream(g).last
    g
  }

  // 6.8.3
  def pruneNetworkVarsAndEdges(Q: Set[RandomVariable[_]], eOpt: Option[CaseX]): BayesianNetwork =
    new BayesianNetwork(this.name, pruneNodes(Q, eOpt, pruneEdges("pruned", eOpt).getGraph))

  def variableEliminationPR(Q: Set[RandomVariable[_]], eOpt: Option[CaseX]): (Factor, BayesianNetwork) = {

    val pruned = pruneNetworkVarsAndEdges(Q, eOpt)
    val R = getRandomVariables.filter(!Q.contains(_)).toSet
    val π = pruned.minDegreeOrder(R)

    val S = π.foldLeft(pruned.getRandomVariables().map(rv => pruned.getCPT(rv).projectRowsConsistentWith(eOpt)).toSet)(
      (S, rv) => {
        val allMentions = S.filter(_.mentions(rv))
        val newS = S -- allMentions
        val T = Factor.multiply(allMentions.toList)
        val Ti = T.sumOut(rv)
        newS + Ti
      })

    (Factor.multiply(S.toList), pruned)
  }

  def variableEliminationMPE(e: CaseX): (Double, BayesianNetwork) = {

    val pruned = pruneEdges("pruned", Some(e))
    val Q = pruned.getRandomVariables()
    val π = pruned.minDegreeOrder(Q.toSet)

    val S = π.foldLeft(Q.map(rv => pruned.getCPT(rv).projectRowsConsistentWith(Some(e))).toSet)(
      (S, rv) => {
        val allMentions = S.filter(_.mentions(rv))
        val newS = S -- allMentions
        val T = Factor.multiply(allMentions.toList)
        val Ti = T.maxOut(rv)
        newS + Ti
      })

    // at this point (since we're iterating over *all* variables in Q)
    // S will contain exactly one trivial Factor

    assert(S.size == 1)

    val sl = S.toList
    val result = sl(0)

    assert(result.numCases() == 1)

    (result.read(result.caseOf(0)), pruned)
  }

  //	public Case variableEliminationMAP(Set Q, Case e)
  //	{
  //		 see ch 6 page 31: Algorithm 8
  //		 TODO
  //      returns an instantiation q which maximizes Pr(q,e) and that probability	
  //
  //	}

  def minDegreeOrder(pX: Set[RandomVariable[_]]): List[RandomVariable[_]] = {

    val X = mutable.Set[RandomVariable[_]]() ++ pX

    val IG = interactionGraph()
    val result = mutable.ListBuffer[RandomVariable[_]]()

    while (X.size > 0) {
      val xVertices = X.map(IG.getGraph.findVertex(_).get)
      IG.getGraph.vertexWithFewestNeighborsAmong(xVertices).map(rvVertex => {
        val rv = rvVertex.getPayload
        result += rv
        IG.eliminate(rv)
        X -= rv
      })
    }
    result.toList
  }

  def minFillOrder(pX: Set[RandomVariable[_]]): List[RandomVariable[_]] = {

    val X = Set[RandomVariable[_]]() ++ pX
    val IG = interactionGraph()
    val result = mutable.ListBuffer[RandomVariable[_]]()

    while (X.size > 0) {
      IG.getGraph.vertexWithFewestEdgesToEliminateAmong(X).map(rv => {
        result += rv
        IG.eliminate(rv)
        X -= rv
      })
    }
    result.toList
  }

  def factorElimination1(Q: Set[RandomVariable[_]]): Factor = {

    val S = mutable.ListBuffer[Factor]() ++ getRandomVariables().map(getCPT(_)).toList

    while (S.size > 1) {

      val fi = S.remove(0)

      val V = fi.getVariables
        .filter(!Q.contains(_))
        .filter(v => S.forall(!_.mentions(v)))
        .toSet

      // At this point, V is the set of vars that are unique to this particular
      // factor, fj, and do not appear in Q

      val fjMinusV = fi.sumOut(V)
      val fj = S.remove(0)
      S += fj.multiply(fjMinusV)
    }

    // there should be one element left in S
    S(0).projectToOnly(Q.toList)
  }

  // the variables Q appear on the CPT for the product of Factors assigned to node r
  def factorElimination2(Q: Set[RandomVariable[_]], τ: EliminationTree, r: EliminationTree#GV): (BayesianNetwork, Factor) = {
    while (τ.g.getVertices().size > 1) {
      // remove node i (other than r) that has single neighbor j in τ
      val fl = τ.g.firstLeafOtherThan(r)

      fl.map(i => {
        val j = τ.getNeighbors(i).iterator.next()
        val ɸ_i = τ.getFactor(i)
        τ.delete(i)
        val V = ɸ_i.getVariables().toSet -- τ.getAllVariables().toSet
        τ.addFactor(j, ɸ_i.sumOut(V))
        τ.draw()
      })
    }
    (foo, τ.getFactor(r).projectToOnly(Q.toList))
  }

  def factorElimination3(Q: Set[RandomVariable[_]], τ: EliminationTree, r: EliminationTree#GV): Factor = {
    // Q is a subset of C_r
    while (τ.g.getVertices().size > 1) {
      // remove node i (other than r) that has single neighbor j in tau
      τ.g.firstLeafOtherThan(r).map(i => {
        val j = τ.getNeighbors(i).iterator.next()
        val ɸ_i = τ.getFactor(i)
        τ.delete(i)
        val Sij = τ.separate(i, j)
        var sijList = mutable.ListBuffer[RandomVariable]()
        sijList ++= Sij
        τ.addFactor(j, ɸ_i.projectToOnly(sijList.toList))
        τ.draw()
      })
    }
    τ.getFactor(r).projectToOnly(Q.toList)
  }

  //	public Map<EliminationTreeNode, Factor> factorElimination(EliminationTree tau, Case e)
  //	{
  //		
  //		for(EliminationTreeNode i : tau.getVertices() ) {
  //
  //			for(RandomVariable E : e.getVariables() ) {
  //
  //				Factor lambdaE = new Factor(E);
  //				// assign lambdaE.E to e.get(E)
  //			}
  //			
  //		}
  //		
  //    TODO EliminationTreeNode r = chooseRoot(tau);
  //		
  //    TODO pullMessagesTowardsRoot();
  //	TODO pushMessagesFromRoot();
  //		
  //		for(EliminationTreeNode i : tau.getVertices()) {
  //			
  //		}
  //		
  //	}

}

