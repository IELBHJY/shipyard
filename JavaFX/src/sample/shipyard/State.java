package sample.shipyard;

public class State {
    //state class
    private int task;
    private int vechileType;
    private State pre;
    private double costs;
    private double time;
    private State D;
    private int[] S;
    private static int sum=Parameter.task;

    public State(int task,int vechileType,State pre,double costs,double time) {
        this.task=task;
        this.vechileType=vechileType;
        this.pre=pre;
        this.costs=costs;
        this.time=time;
        S=new int[sum+2];
    }

    public State clone(){
        State res=new State(task,vechileType,pre,costs,time);
        res.setS(this.S);
        return res;
    }

    public int getVechileType() {
        return vechileType;
    }

    public State getPre() {
        return pre;
    }

    public void setPre(State pre) {
        this.pre = pre;
    }

    public void setVechileType(int vechileType) {
        this.vechileType = vechileType;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int task) {
        this.task = task;
    }

    public double getCosts() {
        return costs;
    }

    public double getTime() {
        return time;
    }

    public State getD() {
        return D;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setD(State d) {
        D = d;
    }

    public void setS(int i,int s) {
        S[i] = s;
    }

    public void setS(int[] s){
        this.S=s.clone();
    }

    public int[] getS() {
        return S;
    }
}