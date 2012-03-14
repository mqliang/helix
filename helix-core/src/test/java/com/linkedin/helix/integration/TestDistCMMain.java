package com.linkedin.helix.integration;

import java.util.Date;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.linkedin.helix.TestHelper;
import com.linkedin.helix.TestHelper.StartCMResult;
import com.linkedin.helix.controller.HelixControllerMain;
import com.linkedin.helix.tools.ClusterStateVerifier;

public class TestDistCMMain extends ZkDistCMTestBase
{
  private static Logger LOG = Logger.getLogger(TestDistCMMain.class);

  @Test
  public void testDistCMMain() throws Exception
  {
    LOG.info("RUN testDistCMMain() at " + new Date(System.currentTimeMillis()));

    // verifyClusters();
    boolean verifyResult = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewVerifier(ZK_ADDR, CONTROLLER_CLUSTER));
    Assert.assertTrue(verifyResult);
    
    verifyResult = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewVerifier(ZK_ADDR, CLUSTER_PREFIX + "_" + CLASS_NAME + "_0"));
    Assert.assertTrue(verifyResult);

    // add more controllers to controller cluster
    for (int i = 0; i < NODE_NR; i++)
    {
      String controller = CONTROLLER_PREFIX + ":" + (NODE_NR + i);
      _setupTool.addInstanceToCluster(CONTROLLER_CLUSTER, controller);
    }
    _setupTool.rebalanceStorageCluster(CONTROLLER_CLUSTER,
                                       CLUSTER_PREFIX + "_" + CLASS_NAME, 10);

    // start extra cluster controllers in distributed mode
    for (int i = 0; i < 5; i++)
    {
      String controller = CONTROLLER_PREFIX + "_" + (NODE_NR + i);

      StartCMResult result = TestHelper.startController(CONTROLLER_CLUSTER,
                                                               controller, ZK_ADDR,
                                                               HelixControllerMain.DISTRIBUTED);
      _startCMResultMap.put(controller, result);
    }

    verifyResult = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewVerifier(ZK_ADDR, CONTROLLER_CLUSTER));
    Assert.assertTrue(verifyResult);
    
    verifyResult = ClusterStateVerifier.verify(
        new ClusterStateVerifier.BestPossAndExtViewVerifier(ZK_ADDR, CLUSTER_PREFIX + "_" + CLASS_NAME + "_0"));
    Assert.assertTrue(verifyResult);

    for (int i = 0; i < NODE_NR; i++)
    {
      stopCurrentLeader(_zkClient, CONTROLLER_CLUSTER, _startCMResultMap);
      
      verifyResult = ClusterStateVerifier.verify(
          new ClusterStateVerifier.BestPossAndExtViewVerifier(ZK_ADDR, CONTROLLER_CLUSTER));
      Assert.assertTrue(verifyResult);
      
      verifyResult = ClusterStateVerifier.verify(
          new ClusterStateVerifier.BestPossAndExtViewVerifier(ZK_ADDR, CLUSTER_PREFIX + "_" + CLASS_NAME + "_0"));
      Assert.assertTrue(verifyResult);
    }

    LOG.info("STOP testDistCMMain() at " + new Date(System.currentTimeMillis()));

  }
}
