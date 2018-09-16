package com.company;

import ilog.concert.IloException;

import java.util.ArrayList;

/**
 * Created by apple on 2018/3/4.
 */
class Solution{
    double epsilon = 0.0001;
    Data data = new Data();
    ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
    ArrayList<ArrayList<Double>> servetimes = new ArrayList<>();
    public Solution(Data data, ArrayList<ArrayList<Integer>> routes, ArrayList<ArrayList<Double>> servetimes) {
        super();
        this.data = data;
        this.routes = routes;
        this.servetimes = servetimes;
    }
    //函数功能：比较两个数的大小
    public int double_compare(double v1,double v2) {
        if (v1 < v2 - epsilon) {
            return -1;
        }
        if (v1 > v2 + epsilon) {
            return 1;
        }
        return 0;
    }
    //函数功能：解的可行性判断
    public void fesible() throws IloException {
        //车辆数量可行性判断
        if (routes.size() > data.vecnum) {
            System.out.println("error: vecnum!!!");
            System.exit(0);
        }
        //车辆载荷可行性判断
        for (int k = 0; k < routes.size(); k++) {
            ArrayList<Integer> route = routes.get(k);
            double capasity = 0;
            for (int i = 0; i < route.size(); i++) {
                capasity += data.demands[route.get(i)];
            }
            if (capasity > data.cap) {
                System.out.println("error: cap!!!");
                System.exit(0);
            }
        }
        //时间窗、车容量可行性判断
        for (int k = 0; k < routes.size(); k++) {
            ArrayList<Integer> route = routes.get(k);
            ArrayList<Double> servertime = servetimes.get(k);
            double capasity = 0;
            for (int i = 0; i < route.size()-1; i++) {
                int origin = route.get(i);
                int destination = route.get(i+1);
                double si = servertime.get(i);
                double sj = servertime.get(i+1);
                if (si < data.a[origin] && si >  data.b[origin]) {
                    System.out.println("error: servertime!");
                    System.exit(0);
                }
                if (double_compare(si + data.dist[origin][destination],data.b[destination]) > 0) {
                    System.out.println(origin + ": [" + data.a[origin] + ","+data.b[origin]+"]"+ " "+ si);
                    System.out.println(destination + ": [" + data.a[destination] + ","+data.b[destination]+"]"+ " "+ sj);
                    System.out.println(data.dist[origin][destination]);
                    System.out.println(destination + ":" );
                    System.out.println("error: forward servertime!");
                    System.exit(0);
                }
                if (double_compare(sj - data.dist[origin][destination],data.a[origin]) < 0) {
                    System.out.println(origin + ": [" + data.a[origin] + ","+data.b[origin]+"]"+ " "+ si);
                    System.out.println(destination + ": [" + data.a[destination] + ","+data.b[destination]+"]"+ " "+ sj);
                    System.out.println(data.dist[origin][destination]);
                    System.out.println(destination + ":" );
                    System.out.println("error: backward servertime!");
                    System.exit(0);
                }
            }
            if (capasity > data.cap) {
                System.out.println("error: cap!!!");
                System.exit(0);
            }
        }
    }
}

