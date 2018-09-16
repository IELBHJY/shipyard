package com.company;

import ilog.concert.IloException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by apple on 2018/3/4.
 */
public class test {
    public static void main(String[] args) throws IloException,IOException {
        long start=System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年-MM月dd日-HH时mm分ss秒");
        Date date = new Date(start);
        System.out.println(formatter.format(date));
        TSP tsp=new TSP();
        tsp.solveModel(41);
        long end=System.currentTimeMillis();
        Date date1 = new Date(end);
        System.out.println(formatter.format(date1));
        //SP_TimeConstraint sp=new SP_TimeConstraint();
        //sp.buildModel();
        //CG cg=new CG();
        //cg.solveModel();
        /*IloCplex model=new IloCplex();
        model.setOut(null);
        IloNumVar numVar1=model.numVar(0,Integer.MAX_VALUE,IloNumVarType.Int,"x1");
        IloNumVar numVar2=model.numVar(0,Integer.MAX_VALUE,IloNumVarType.Int,"x2");
        IloNumExpr numExpr;
        numExpr=model.sum(model.prod(40,numVar1),model.prod(90,numVar2));
        model.addMaximize(numExpr);
        IloNumExpr numExpr1;
        numExpr1=model.sum(model.prod(9,numVar1),model.prod(7,numVar2));
        model.addLe(numExpr1,56);
        IloNumExpr numExpr2;
        numExpr2=model.sum(model.prod(7,numVar1),model.prod(20,numVar2));
        model.addLe(numExpr2,70);
        if(model.solve()){
            System.out.println(model.getObjValue());
            System.out.println(model.getValue(numVar1)+"  "+model.getValue(numVar2));*/
        }
    }
