module newssite.strategies;
import lib "newssiteTactics.s";

define boolean styleApplies = Model.hasType(M, "ClientT") && Model.hasType(M, "ServerT");
define boolean cViolation = exists c : T. ClientT in M.components | c.experRespTime > M.MAX_RESPTIME;
define set servers = {select s : T.ServerT in M.components | true};
define set unhappyClients = {select c : T.ClientT in M.components | c.experRespTime > M.MAX_RESPTIME};
define int numClients = Set.size({select c : T.ClientT in M.components | true});
define int numUnhappy = Set.size(unhappyClients);
define float numUnhappyF = 1.0 * numUnhappy;

define boolean hiLoad = exists s : T.ServerT in M.components | s.load > M.MAX_UTIL;
define boolean hiRespTime = exists c : T. ClientT in M.components | c.experRespTime > M.MAX_RESPTIME;
define boolean lowRespTime = exists c : T.ClientT in M.components | c.experRespTime < M.MIN_RESPTIME;
define float totalCost = Model.sumOverProperty("cost", servers);
define boolean hiCost = totalCost >= M.THRESHOLD_COST;
define float avgFidelity = Model.sumOverProperty("fidelity", servers) / Set.size(servers);
define boolean lowFi = avgFidelity < M.THRESHOLD_FIDELITY;


// While it encounters high experienced response time, this simple strategy
// first enlists one new server, then lowers fidelity one step, then quits
strategy SimpleReduceResponseTime
[ 1 ]
{
    t0: (default) −> lowerFidelity (2 , 100) @[3000 /*ms*/ ];
}