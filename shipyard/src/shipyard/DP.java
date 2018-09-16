package shipyard;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DP {
    int findRoute;
    List<Integer>[] route;
    int[] vechile;
    Data data;
    double[] price1;
    double[] price2;
    public DP(int findRoute,Data data,double[] price11,double[] price22) {
        this.findRoute = findRoute;
        route=new List[findRoute];
        vechile=new int[findRoute];
        for(int i=0;i<findRoute;i++){
            route[i]=new ArrayList<>();
        }
        price1=new double[data.n-1];
        price2=new double[data.t-1];
        this.data=data;
        this.price1=price11;
        this.price2=price22;
    }

    public void findRoutes(){
        // start point is 0 ,end point is H+1
        int sum=0;
        for(int t=1;t<data.t;t++) {
            State start = new State(0, t,null,0.0,0.0);
            start.setS(0,1);
            State end=new State(data.n, t,null,0.0,0.0);
            end.setS(data.n,1);
            Stack<State> stack = new Stack<>();
            stack.push(start);
            while(!stack.isEmpty()){
                State top=stack.pop();
                //判断此时是否可以结束，如果结束，更新信息
                if(top.getTask()>0){
                    double cost=top.getCosts();
                    cost+=data.w[top.getTask()][0];
                    if(cost<0){
                        vechile[sum]=t;
                        List<Integer> list=new ArrayList<>();
                        State tmp=top;
                        while(tmp.getPre()!=start){
                            list.add(tmp.getTask());
                            tmp=tmp.getPre();
                        }
                        route[sum]=list;
                        sum++;
                        if(sum==findRoute) {
                            break;
                        }
                    }
                }
                for(int i=1;i<data.n;i++){
                    if(top.getS()[i]==1 || data.taskWeights[i]>data.truckCaptitys[t]
                            || top.getTime()+data.w[top.getTask()][i]>data.lateTime[i]) continue;
                    double cost=top.getCosts()+data.w[top.getTask()][i];
                    cost-=price1[i-1]-price2[t-1];
                    double time=top.getTime()+data.w[top.getTask()][i];
                    if(time<data.earlyTime[i]){
                        time=data.earlyTime[i]+data.carryTaskTime[i];
                    }else{
                        time=time+data.carryTaskTime[i];
                    }
                    State state=new State(i,t,top,cost,time);
                    state.setS(top.getS());
                    state.setS(i,1);
                    stack.push(state);
                }
            }
        }

    }

    public List<Integer>[] getRoute() {
        return route;
    }

    public int[] getVechile() {
        return vechile;
    }
}
