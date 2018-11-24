package FindPath;

import java.util.List;

public class Main {

    public static void main(String[] args){
        int[][] adj={{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                     {0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
                     {0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
                     {0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
                     {0,0,1,1,0,1,1,0,0,0,0,0,0,0,0,0,0},
                     {0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0},
                     {0,0,0,0,1,0,0,1,0,0,0,1,0,0,0,0,0},
                     {0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0},
                     {0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0},
                     {0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0},
                     {0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0},
                     {0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,1},
                     {0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
                     {0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0},
                     {0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0},
                     {0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1},
                     {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0},
                };
        try {
            FindBestPath findBestPath = new FindBestPath(16, adj);
            List<Integer> ans;
            double[] cost={0.0};
            ans=findBestPath.findShortPath("车场","8号平台",cost);
            System.out.println(ans);
            System.out.println(cost[0]/5);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
