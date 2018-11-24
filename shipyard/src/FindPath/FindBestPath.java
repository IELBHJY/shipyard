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
        penty_turn=3;
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

    public List<Integer> findShortPath(String start,String end,double[] cost) throws Exception{
        double[] cost1={0.0,0.0};
        int[] points=findPoints(start,end,cost1);
        int source=points[0];
        int target=points[1];
        if(source==target) return new ArrayList<>();
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
        sum+=cost1[0]+cost1[1];
        cost[0]=sum;
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

    public int[] findPoints(String start,String end,double[] cost) throws Exception{
        int[] ans=new int[2];
        db.connection();
        ResultSet rs=db.search("SELECT * FROM shipyard.storage where storage_name='"+start+"'");
        rs.last();
        int size=rs.getRow();
        if(size==0){
            System.out.println("start:"+start);
            //System.exit(0);
        }
        rs.beforeFirst();
        double x_start=0;
        double y_start=0;
        while (rs.next()) {
            x_start = Double.parseDouble(rs.getString("xcoordinate"));
            y_start = Double.parseDouble(rs.getString("ycoordinate"));
        }
        rs=db.search("SELECT * FROM storage where storage_name='"+end+"'");
        rs.last();
        size=rs.getRow();
        if(size==0){
            System.out.println("end:"+end);
            //System.exit(0);
        }
        rs.beforeFirst();
        double x_end=0;
        double y_end=0;
        while (rs.next()) {
            x_end = Double.parseDouble(rs.getString("xcoordinate"));
            y_end = Double.parseDouble(rs.getString("ycoordinate"));
        }
        db.close();
        double min=Double.MAX_VALUE;
        double min1=Double.MAX_VALUE;
        for(int i=1;i<=n;i++){
            double temp=Math.sqrt(Math.pow(x_start-xcoordinate[i],2)+Math.pow(y_start-ycoordinate[i],2));
            if(temp<min){
                min=temp;
                ans[0]=i;
            }
            double temp1=Math.sqrt(Math.pow(x_end-xcoordinate[i],2)+Math.pow(y_end-ycoordinate[i],2));
            if(temp1<min1){
                min1=temp1;
                ans[1]=i;
            }
        }
        cost[0]=min;
        cost[1]=min1;
        return ans;
    }


}
