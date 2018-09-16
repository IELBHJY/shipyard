package com.company;

import ilog.concert.*;
import ilog.cplex.IloCplex;

/**
 * Created by apple on 2018/3/20.
 */
public class duals {
    public static void main(String[] args) throws IloException
    {
        // Create the modeler/solver object
        IloCplex cplex = new IloCplex();

        IloNumVar[][] var = new IloNumVar[1][];
        IloRange[][] rng = new IloRange[1][];

        // Evaluate command line option and call appropriate populate
        // method.
        // The created ranges and variables are returned as element 0 of
        // arrays
        // var and rng.

        populateByRow(cplex, var, rng);
        // populateByColumn(cplex, var, rng);
        //populateByNonzero(cplex, var, rng);

        // write model to file,保存模型
        //cplex.exportModel("lpex1.lp");

        // solve the model and display the solution if one was found
        if (cplex.solve())
        {
            //var[0] = x,计算后各变量的取值
            double[] x = cplex.getValues(var[0]);
            double[] dj = cplex.getReducedCosts(var[0]);
            //对偶
            double[] pi = cplex.getDuals(rng[0]);
            double[] slack = cplex.getSlacks(rng[0]);
            //cplex.setWarning(System.err);
            cplex.output().println("totalTime = "+cplex.getCplexTime());

            cplex.output()
                    .println("Solution status = " + cplex.getStatus());
            //f(x)的结果
            cplex.output()
                    .println("Solution value  = " + cplex.getObjValue());

            int nvars = x.length;
            for (int j = 0; j < nvars; ++j)
            {
                cplex.output().println("Variable " + j + ": Value = " + x[j]
                        + " Reduced cost = " + dj[j]);
            }

            int ncons = slack.length;
            for (int i = 0; i < ncons; ++i)
            {
                cplex.output().println("Constraint " + i + ": Slack = "
                        + slack[i] + " Pi = " + pi[i]);
            }
        }
        cplex.end();
    }

    static void populateByRow(IloMPModeler model, IloNumVar[][] var,
                              IloRange[][] rng) throws IloException
    {
        //变量取值下界
        double[] lb = { 0.0, 0.0, 0.0 };
        //变量取值上界
        double[] ub = { 40.0, Double.MAX_VALUE, Double.MAX_VALUE };
        //变量名
        String[] varname = { "x1", "x2", "x3" };
        //初始化变量,3是变量个数
        IloNumVar[] x = model.numVarArray(3, lb, ub, varname);
        var[0] = x;

        //变量前系数
        //y = x1 + 2x2 + 3x3
        //计算y的最大值
        double[] objvals = { 1.0, 2.0, 3.0 };
        model.addMaximize(model.scalProd(x, objvals));

        //约束条件
        rng[0] = new IloRange[2];
        //c1
        //–x1 + x2 . + x3 ≤ 20
        rng[0][0] = model.addLe(model.sum(model.prod(-1.0, x[0]),
                model.prod(1.0, x[1]), model.prod(1.0, x[2])), 20.0, "c1");
        //c2
        //x1 – 3x2 + x3 ≤ 30
        rng[0][1] = model.addLe(model.sum(model.prod(1.0, x[0]),
                model.prod(-3.0, x[1]), model.prod(1.0, x[2])), 30.0, "c2");
    }

    static void populateByColumn(IloMPModeler model, IloNumVar[][] var,
                                 IloRange[][] rng) throws IloException
    {
        IloObjective obj = model.addMaximize();
        rng[0] = new IloRange[2];
        rng[0][0] = model.addRange(-Double.MAX_VALUE, 20.0, "c1");
        rng[0][1] = model.addRange(-Double.MAX_VALUE, 30.0, "c2");
        IloRange r0 = rng[0][0];
        IloRange r1 = rng[0][1];
        var[0] = new IloNumVar[3];
        var[0][0] = model.numVar(
                model.column(obj, 1.0)
                        .and(model.column(r0, -1.0).and(model.column(r1, 1.0))),
                0.0, 40.0, "x1");
        var[0][1] = model.numVar(
                model.column(obj, 2.0)
                        .and(model.column(r0, 1.0).and(model.column(r1, -3.0))),
                0.0, Double.MAX_VALUE, "x2");
        var[0][2] = model.numVar(
                model.column(obj, 3.0)
                        .and(model.column(r0, 1.0).and(model.column(r1, 1.0))),
                0.0, Double.MAX_VALUE, "x3");
    }

    static void populateByNonzero(IloMPModeler model, IloNumVar[][] var,
                                  IloRange[][] rng) throws IloException
    {
        double[] lb = { 0.0, 0.0, 0.0 };
        double[] ub = { 40.0, Double.MAX_VALUE, Double.MAX_VALUE };
        IloNumVar[] x = model.numVarArray(3, lb, ub);
        var[0] = x;

        double[] objvals = { 1.0, 2.0, 3.0 };
        model.add(model.maximize(model.scalProd(x, objvals)));

        rng[0] = new IloRange[2];
        rng[0][0] = model.addRange(-Double.MAX_VALUE, 20.0);
        rng[0][1] = model.addRange(-Double.MAX_VALUE, 30.0);

        rng[0][0].setExpr(model.sum(model.prod(-1.0, x[0]),
                model.prod(1.0, x[1]), model.prod(1.0, x[2])));
        rng[0][1].setExpr(model.sum(model.prod(1.0, x[0]),
                model.prod(-3.0, x[1]), model.prod(1.0, x[2])));
        x[0].setName("x1");
        x[1].setName("x2");
        x[2].setName("x3");
        rng[0][0].setName("c1");
        rng[0][1].setName("c2");
    }
}
