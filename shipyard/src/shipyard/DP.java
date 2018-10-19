package shipyard;

import java.util.*;

public class DP {
    int findRoute;
    HashMap<Integer,List<Integer>> route;
    HashMap<Integer,Integer> vechile;
    double[] costs;
    int[] indexs;
    Data data;
    double[] price1;
    double[] price2;
    int[] max_per_truck;
    HashMap<Integer,List<Integer>> paths;
    int[][] tabuEdges;
    /*
    * 找路径的时候，需要知道路径集合里已经存在的路径，以及分支之后，
    * 不能走的路径和必须走的路径，可以加速 find paths。
    * */
    public DP(int findRoute,Data data,double[] price11,double[] price22,HashMap<Integer,List<Integer>> paths,int[][] tabuEdges) {
        this.findRoute = findRoute;
        route=new HashMap<>();
        vechile=new HashMap<>();
        costs=new double[findRoute];
        indexs=new int[findRoute];
        price1=new double[price11.length];
        price2=new double[price22.length];
        this.data=data;
        this.price1=price11;
        this.price2=price22;
        max_per_truck=new int[]{5,2,2,2,2};
        this.paths=paths;
        this.tabuEdges=new int[tabuEdges.length][tabuEdges.length];
        for(int i=0;i<tabuEdges.length;i++) {
            this.tabuEdges[i] = tabuEdges[i].clone();
        }
    }

    /*
    * 找路径，paths（路径集合）里面存在的路径不再需要，tabuEdges里面被禁止的边不能出现，必须走的路径必须出现。
    * */
    public boolean findRoutes(){
        int sum=1;
        Queue<State> queue=new PriorityQueue<>(new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return Double.compare(o1.getCosts(),o2.getCosts());
            }
        });
        for(int t=1;t<data.t;t++) {
            if(sum>findRoute/data.t){
                break;
            }
            int sum1=0;
            State start = new State(0, t,null,0.0,0.0);
            start.setS(0,1);
            State end=new State(data.n, t,null,0.0,0.0);
            end.setS(data.n,1);
            Queue<State> stack=new PriorityQueue<>(new Comparator<State>() {
                @Override
                public int compare(State o1, State o2) {
                    return Double.compare(o1.getCosts(),o2.getCosts());
                }
            });
            stack.offer(start);
            while(!stack.isEmpty()){
                if(sum1>findRoute/data.t){
                    break;
                }
                State top=stack.poll();
                if(top.getTask()>0){
                    double cost=top.getCosts();
                    if(cost + data.w[top.getTask()][0] < 0){
                        State tmp=top.clone();
                        tmp.setCosts(cost + data.w[top.getTask()][0]);
                        List<Integer> list=new ArrayList<>();
                        State temp=tmp;
                        while(temp.getPre()!=null){
                            list.add(temp.getTask());
                            temp=temp.getPre();
                        }
                        //判断路径是否已经在路径集合中存在
                        if(isContain(list)){

                        }else {
                            queue.offer(tmp);
                            sum1++;
                            sum++;
                        }
                    }
                }
                for(int i=1;i<data.n;i++){
                    if(top.getS()[i]==1 || data.taskWeights[i]>data.truckCaptitys[t]
                            || top.getTime()+data.w[top.getTask()][i]>data.lateTime[i]
                            || tabuEdges[top.getTask()][i]==-1) {
                        continue;
                    }
                    double cost=top.getCosts()+data.w[top.getTask()][i];
                    cost-=price1[i]+price2[t];
                    double time=top.getTime()+data.w[top.getTask()][i];
                    if(time<data.earlyTime[i]){
                        time=data.earlyTime[i]+data.carryTaskTime[i];
                    }else if(time>= data.earlyTime[i] && time<=data.lateTime[i]){
                        time+=data.carryTaskTime[i];
                    } else{
                        continue;
                    }
                    State state=new State(i,t,top,cost,time);
                    state.setS(top.getS());
                    state.setS(i,1);
                    stack.offer(state);
                }
            }
        }
        System.out.println("一共找到："+sum+" 条可行路径");
        if(sum<findRoute){
            return false;
        }else{
            for(int i=0;i<findRoute;i++){
                State temp=queue.poll();
                if(temp==null){
                    continue;
                }
                vechile.put(i,temp.getVechileType());
                List<Integer> list=new ArrayList<>();
                while(temp.getPre()!=null){
                    list.add(temp.getTask());
                    temp=temp.getPre();
                }
                route.put(i,list);
            }
        }
        return true;
    }

    public int findAllRoutes(){
        int sum=0;
        Queue<State> queue=new PriorityQueue<>(new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return Double.compare(o1.getCosts(),o2.getCosts());
            }
        });
        for(int t=1;t<data.t;t++) {
            State start = new State(0, t,null,0.0,0.0);
            start.setS(0,1);
            State end=new State(data.n, t,null,0.0,0.0);
            end.setS(data.n,1);
            Queue<State> stack=new PriorityQueue<>(new Comparator<State>() {
                @Override
                public int compare(State o1, State o2) {
                    return Double.compare(o1.getCosts(),o2.getCosts());
                }
            });
            stack.offer(start);
            while(!stack.isEmpty()){
                State top=stack.poll();
                if(top.getTask()>0){
                    double cost=top.getCosts();
                    if(cost+data.w[top.getTask()][0] < 0){
                        State temp=top.clone();
                        temp.setCosts(cost+data.w[top.getTask()][0]);
                        queue.offer(temp);
                        sum++;
                    }
                }
                for(int i=1;i<data.n;i++){
                    if(top.getS()[i]==1 || data.taskWeights[i]>data.truckCaptitys[t]
                            || top.getTime()+data.w[top.getTask()][i]>data.lateTime[i]) {continue;}
                    double cost=top.getCosts()+data.w[top.getTask()][i];
                    cost-=price1[i]+price2[t];
                    double time=top.getTime()+data.w[top.getTask()][i];
                    if(time<data.earlyTime[i]){
                        time=data.earlyTime[i]+data.carryTaskTime[i];
                    }else if(time<=data.lateTime[i] && time>=data.earlyTime[i]){
                        time=time+data.carryTaskTime[i];
                    } else{
                        continue;
                    }
                    State state=new State(i,t,top,cost,time);
                    state.setS(top.getS());
                    state.setS(i,1);
                    stack.offer(state);
                }
            }
        }
        System.out.println("一共找到："+sum+" 条可行路径");
        findRoute=sum;
        for(int i=0;i<findRoute;i++){
            State temp=queue.poll();
            vechile.put(i,temp.getVechileType());
            List<Integer> list=new ArrayList<>();
            while(temp.getPre()!=null){
                list.add(0,temp.getTask());
                temp=temp.getPre();
            }
            route.put(i,list);
        }
        return sum;
    }

    public List<Integer>[] getRoute() {
        List<Integer>[] ans=new List[findRoute];
        for(int i=0;i<findRoute;i++){
            ans[i]=route.get(i);
        }
        return ans;
    }

    public int[] getVechile() {
        int[] ans=new int[findRoute];
        for(int i=0;i<findRoute;i++){
            ans[i]=vechile.get(i);
        }
        return ans;
    }

    private boolean isContain(List<Integer> list){
        boolean res=false;
        for(Integer key:this.paths.keySet()){
            List<Integer> value=paths.get(key);
            if(value.size()!=list.size()){
                continue;
            }else{
                if(list.containsAll(value)){
                    return true;
                }
            }
        }
        return res;
    }

}
