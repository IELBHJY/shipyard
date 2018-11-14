package FindPath;

import Database.ConnectDB;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class FindBestPath {
     int n;
     double penty_turn;
     HashMap<Integer,List<Integer>> res;
     List<Integer>[] adj;
     int[] visited;
     Stack<Integer> stack;
     ConnectDB db;
     double[] xcoordinate;
     double[] ycoordinate;
    /***
     *
     * @param n 几个点
     * @param adj 邻接矩阵
     */
    public FindBestPath(int n,int[][] adj) throws Exception{
        this.n=n;
        this.adj=new List[n+1];
        penty_turn=10;
        for(int i=1;i<=n;i++){
            this.adj[i]=new ArrayList<>();
        }
        for(int i=1;i<=n;i++){
            for(int j=i+1;j<=n;j++){
                if(adj[i][j]==1){
                    this.adj[i].add(j);
                    this.adj[j].add(i);
                }
            }
        }
        this.visited=new int[n+1];
        xcoordinate=new double[n+1];
        ycoordinate=new double[n+1];
        db=new ConnectDB();
        db.connection();
        ResultSet rs=db.query("roads");
        while (rs.next()){
            String id=rs.getString("ID");
            String x=rs.getString("横坐标");
            String y=rs.getString("纵坐标");
            xcoordinate[Integer.parseInt(id)]=Double.parseDouble(x);
            ycoordinate[Integer.parseInt(id)]=Double.parseDouble(y);
        }
        db.close();
    }

    public HashMap<Integer,List<Integer>> findAllPaths(int source,int target){
        res=new HashMap<>();
        stack=new Stack<>();
        dfs(source,target);
        return res;
    }

    private void dfs(int source,int target){
       stack.push(source);
       visited[source]=1;
       for(int i=1;i<=n;i++){
           if(source==target){
               List<Integer> list=new ArrayList<>();
               for(int j=0;j<stack.size();j++){
                   list.add(stack.get(j));
               }
               if(res.keySet().size()==0){
                   res.put(1,list);
               }else{
                   int size=res.keySet().size();
                   res.put(size+1,list);
               }
               stack.pop();
               visited[source]=0;
               break;
           }
           if(visited[i]==0 && adj[source].contains(i)){
               dfs(i,target);
           }
           if(i==n){
               stack.pop();
               visited[source]=0;
           }
       }
    }

    public List<Integer> findShortPath(int source,int target){
        HashMap<Integer,List<Integer>> res=findAllPaths(source,target);
        List<Integer> ans=new ArrayList<>();
        double sum=Double.MAX_VALUE;
        for(Integer key:res.keySet()){
            double temp=0.0;
            List<Integer> path=res.get(key);
            for(int i=0;i<path.size()-1;i++){
                temp+=Math.sqrt(Math.pow((xcoordinate[path.get(i)]-xcoordinate[path.get(i+1)]),2)+Math.pow((ycoordinate[path.get(i)]-ycoordinate[path.get(i+1)]),2));
            }
            int turn_sum=calTurns(path);
            temp+=penty_turn*turn_sum;
            if(temp<sum){
                sum=temp;
                ans=res.get(key);
            }
        }
        return ans;
    }
    
    private int calTurns(List<Integer> list){
        int ans=0;
        if(list.size()<=2) return ans;
        for(int i=0;i<list.size()-2;i++){
            if(xcoordinate[list.get(i)]==xcoordinate[list.get(i+1)] && ycoordinate[list.get(i+1)]==ycoordinate[list.get(i+2)]){
                ans++;
            }
            if(ycoordinate[list.get(i)]==ycoordinate[list.get(i+1)] && xcoordinate[list.get(i+1)]==xcoordinate[list.get(i+2)]){
                ans++;
            }
        }
        return ans;
    }



}
