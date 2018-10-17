package shipyard;

import ilog.concert.IloException;

import java.util.*;


/***
 * 该类作为分支定价算法框架中的分支定界
 */
public class BranchAndBound {

    final double INF=Double.MAX_VALUE;
    private int task;
    private int truck;
    private Data data;
    private Double upperBound=Double.MAX_VALUE;
    CG mp;
    /*
     * 搜索树里面存储主模型，但是不建模，只把相关的参数矩阵存储。
     */
    private Queue<CG> searchTree;


    public BranchAndBound(int task,int truck,Data data) {
        this.task=task;
        this.truck=truck;
        this.data=data;
        searchTree=new PriorityQueue<>(new Comparator<CG>() {
            @Override
            public int compare(CG o1, CG o2) {
                return Double.compare(o1.getBest(),o2.getBest());
            }
        });

    }

    /**
     * 该函数是初始化主问题
     * 后期可以通过该函数初始化主问题
     * @throws IloException
     */
    private void firstMPModel() throws IloException {
         mp=new CG(task,truck,task,data);
         mp.creatInitalSolution();
         mp.Solve();
         if(mp.getInteger()) {
             upperBound = mp.getBest();
         }
        //处理根结点，先建立根结点，然后判断是否是整数，分支，子树进队列
        if(mp.getInteger()) return;
        int[] edge=branch(mp);
        //根据mp重新建立左右子树
        CG left=new CG(task,truck,mp.getPaths().keySet().size(),data);
        updateLeftModel(edge,left,mp);
        CG right=new CG(task,truck,mp.getPaths().keySet().size(),data);
        updateRightModel(edge,right,mp);
        searchTree.offer(left);
        searchTree.offer(right);
     }
    /***
     * 更新左模型的信息，即将用于建模的所有信息都更新好，但不建模
     * 左模型是设定最优解中包含分支边，因此，需要将分支边a->b，a到其他点的cost设为无限大
     * 将其他点到b的cost设为无限大即可
     * @param edge
     * @param left
     */
     private void updateLeftModel(int[] edge,CG left,CG mp) throws IloException{
          int [][] a=mp.getA();
          int[] b=mp.getB();
          double[] c=mp.getC();
          HashMap<Integer,List<Integer>> list=mp.getPaths();
          for(Integer key:list.keySet()){
              List<Integer> path=list.get(key);
              int start=edge[0];
              int end=edge[1];
              if(!path.contains(start) && !path.contains(end)) continue;
              if(path.contains(start)){
                  int index=path.indexOf(start);
                  if(index<path.size()-1){
                      int next=path.get(index+1);
                      if(next!=end){
                          c[key]=INF;
                      }
                  }
              }
              if(path.contains(end)){
                   int index=path.indexOf(end);
                   if(index>0){
                       int before=path.get(index-1);
                       if(before!=start){
                           c[key]=INF;
                       }
                   }
              }
          }
          left.initalModelInfor(mp.getPaths(),mp.getTrucks(),a,c,b);
          left.setBest(mp.getBest());
          int[][] tabuEdges=mp.getTabuEdges();
          left.setTabuEdges(tabuEdges);
          //edge[0]-->edge[1]边必走
          left.setTabuEdges(edge[0],edge[1],1);
          for(int i=1;i<=task;i++){
             if(i!=edge[1]){
                 left.setTabuEdges(edge[0],i,-1);
             }
             if(i!=edge[0]){
                 left.setTabuEdges(i,edge[1],-1);
             }
          }
     }

    /***
     * 更新右模型的信息，即将用于建模的所有信息都更新好，但不建模
     * 右模型是设定最优解中不包含分支边，因此，需要将分支边a->b，a到b的费用设为无限大即可
     * @param edge
     * @param right
     */
     private void updateRightModel(int[] edge,CG right,CG mp) throws IloException{
         int [][] a=mp.getA();
         int[] b=mp.getB();
         double[] c=mp.getC();
         HashMap<Integer,List<Integer>> list=mp.getPaths();
         for(Integer key:list.keySet()){
             List<Integer> path=list.get(key);
             int start=edge[0];
             int end=edge[1];
             if(path.contains(start)){
                 int index=path.indexOf(start);
                 if(index<path.size()-1){
                     if(path.get(index+1)==end){
                         c[key]=INF;
                     }
                 }
             }
         }
         right.initalModelInfor(mp.getPaths(),mp.getTrucks(),a,c,b);
         right.setBest(mp.getBest());
         //edge[0]-->>edge[1]设定不走既可
         int[][] tabuEdges=mp.getTabuEdges();
         right.setTabuEdges(tabuEdges);
         right.setTabuEdges(edge[0],edge[1],-1);
     }

     /**
      * 判断解是否是整数，如果不是整数，需要分支，并完成分支，然后初始化两个分支后的主问题，加入主问题队列
      * 需要根据新的分支问题，更新主问题模型，调用cg求解模型
      * */
     public void Solve() throws IloException{
         firstMPModel();
         while(!searchTree.isEmpty()) {
             CG top = searchTree.poll();
             top.buildMPModel();
             top.solveMPModel();
             if(top.getInteger() && top.getBest()<upperBound){
                 upperBound=top.getBest();
             }
             if(!top.getInteger()) {
                 int[] edge = branch(top);
                 System.out.println("branch edge is:" + edge[0] + "-" + edge[1]);
                 //根据top重新建立左右子树
                 CG left = new CG(task, truck, top.getPaths().keySet().size(), data);
                 updateLeftModel(edge, left,top);
                 CG right = new CG(task, truck, top.getPaths().keySet().size(), data);
                 updateRightModel(edge, right,top);
                 searchTree.offer(left);
                 searchTree.offer(right);
             }
         }
         System.out.println(upperBound);
     }

    /***
     * 返回分支的弧
     * @param mp 主问题模型
     * @return
     */
     public int[] branch(CG mp){
         HashMap<Integer,List<Integer>> paths=mp.bestPaths;
         List<Integer> num=new ArrayList<>(paths.keySet());
         int[] ans=new int[2];
         for(int i=0;i<num.size();i++){
             List<Integer> list=paths.get(num.get(i));
             for(Integer task:list){
                 for(int j=i+1;j<num.size();j++){
                     List<Integer> list1=paths.get(num.get(j));
                     if(list1.contains(task)){
                         int index=list1.indexOf(task);
                         if(index<list1.size()-1){
                             ans[0]=task;
                             ans[1]=list1.get(index+1);
                             return ans;
                         }
                     }
                 }
             }
         }
         return ans;
     }
}
