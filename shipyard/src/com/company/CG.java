package com.company;

import ilog.concert.*;
import ilog.cplex.IloCplex;

/**
 * Created by apple on 2018/3/19.
 */
public class CG {
    static double RC_EPS = 1.0e-6;
    static double   _rollWidth;
    static double[] _size;//需求尺寸
    static double[] _amount;//需求数量
    public static void main(String[] args) throws IloException{
        /*IloCplex model=new IloCplex();
        IloNumVar[] x=new IloNumVar[2];
        for(int i=0;i<2;i++){
            x[i]=model.numVar(0,Integer.MAX_VALUE);
        }
        IloLinearNumExpr obj=model.linearNumExpr();
        int[] c=new int[2];
        c[0]=2;c[1]=3;
        for(int i=0;i<2;i++){
            obj.addTerm(c[i],x[i]);
        }
        model.addMaximize(obj);
        IloLinearNumExpr expr=model.linearNumExpr();
        c=new int[2];
        c[0]=1;c[1]=2;
        //c1[0]=5;c1[1]=4;c1[2]=2;c1[3]=2;c1[4]=1;c1[5]=1;
        for(int i=0;i<2;i++){
            expr.addTerm(c[i],x[i]);
        }
        IloRange iloRange=model.addRange(0,expr,8);
        IloLinearNumExpr expr1=model.linearNumExpr();
        c=new int[2];
        c[0]=4;c[1]=0;
        //c[0]=0;c[1]=1;c[2]=2;c[3]=0;c[4]=1;c[5]=3;
        for(int i=0;i<2;i++){
            expr1.addTerm(c[i],x[i]);
        }
        IloRange iloRange1=model.addRange(0,expr1,16);
        IloLinearNumExpr expr2=model.linearNumExpr();
        //c[0]=0;c[1]=0;c[2]=0;c[3]=1;c[4]=1;c[5]=0;
        c[0]=0;c[1]=4;
        for(int i=0;i<2;i++){
            expr2.addTerm(c[i],x[i]);
        }
        IloRange iloRange2=model.addRange(0,expr2,12);
        /*
        //约束1
        IloLinearNumExpr expr=model.linearNumExpr();
        c=new int[2];
        c[0]=1;c[1]=2;
        //c1[0]=5;c1[1]=4;c1[2]=2;c1[3]=2;c1[4]=1;c1[5]=1;
        for(int i=0;i<2;i++){
            expr.addTerm(c[i],x[i]);
        }
        model.addLe(expr,8);
        //约束2
        IloLinearNumExpr expr1=model.linearNumExpr();
        c=new int[2];
        c[0]=4;c[1]=0;
        //c[0]=0;c[1]=1;c[2]=2;c[3]=0;c[4]=1;c[5]=3;
        for(int i=0;i<2;i++){
            expr1.addTerm(c[i],x[i]);
        }
        model.addLe(expr1,16);
        //yueshu3
        IloLinearNumExpr expr2=model.linearNumExpr();
        //c[0]=0;c[1]=0;c[2]=0;c[3]=1;c[4]=1;c[5]=0;
        c[0]=0;c[1]=4;
        for(int i=0;i<2;i++){
            expr2.addTerm(c[i],x[i]);
        }
        model.addLe(expr2,12);
        model.solve();
        System.out.println(model.getObjValue());
        for(int i=0;i<2;i++){
            System.out.print(model.getValue(x[i])+" ");
        }
        double dual=model.getDual(iloRange);
        double dual1=model.getDual(iloRange1);
        double dual2=model.getDual(iloRange2);
        System.out.println(dual+" "+dual1+" "+dual2);*/
        _rollWidth=17;
        _size=new double[3];
        _size[0]=3;
        _size[1]=5;
        _size[2]=9;
        _amount=new double[3];
        _amount[0]=25;
        _amount[1]=20;
        _amount[2]=15;
        IloCplex cutSolver = new IloCplex();
        cutSolver.setOut(null);
        IloObjective RollsUsed = cutSolver.addMinimize();
        IloRange[]   Fill = new IloRange[_amount.length];
        //IloRange[] Fill
        for (int f = 0; f < _amount.length; f++ ) {
            Fill[f] = cutSolver.addRange(_amount[f], Double.MAX_VALUE);
        }
        IloNumVarArray Cut = new IloNumVarArray();
        int nWdth = _size.length;
        for (int j = 0; j < nWdth; j++)
            Cut.add(cutSolver.numVar(cutSolver.column(RollsUsed, 1.0).and(
                    cutSolver.column(Fill[j],
                            (int)(_rollWidth/_size[j]))),
                    0.0, Double.MAX_VALUE));//添加一个变量x1。
        cutSolver.setParam(IloCplex.Param.RootAlgorithm, IloCplex.Algorithm.Primal);
        for(int count=0;count<5;count++) {
            cutSolver.solve();
            report1(cutSolver, Cut, Fill);
            IloCplex patSolver = new IloCplex();
            patSolver.setOut(null);
            IloObjective ReducedCost = patSolver.addMinimize();
            IloNumVar[] Use = patSolver.numVarArray(nWdth,
                    0., Double.MAX_VALUE,
                    IloNumVarType.Int);// a3  a5  a9
            patSolver.addRange(-Double.MAX_VALUE,
                    patSolver.scalProd(_size, Use),
                    _rollWidth);//添加约束
            /// FIND AND ADD A NEW PATTERN ///
            double[] newPatt;
            double[] price = cutSolver.getDuals(Fill);//获取影子价格
            ReducedCost.setExpr(patSolver.diff(1.,
                    patSolver.scalProd(Use, price)));//更新目标函数
            patSolver.solve();
            report2(patSolver, Use);
            if ( patSolver.getObjValue() > -RC_EPS )
                break;
            newPatt = patSolver.getValues(Use);//更新新生成的列
            IloColumn column = cutSolver.column(RollsUsed, 1.);
            for (int p = 0; p < newPatt.length; p++)
                column = column.and(cutSolver.column(Fill[p], newPatt[p]));
            Cut.add(cutSolver.numVar(column, 0., Double.MAX_VALUE));
        }
        for ( int i = 0; i < Cut.getSize(); i++ ) {
            cutSolver.add(cutSolver.conversion(Cut.getElement(i),
                    IloNumVarType.Int));
        }
        cutSolver.solve();
        report3 (cutSolver, Cut);
        System.out.println("Solution status: " + cutSolver.getStatus());
        cutSolver.end();
    }
    static class IloNumVarArray {
        int _num           = 0;
        IloNumVar[] _array = new IloNumVar[32];
        void add(IloNumVar ivar) {
            if ( _num >= _array.length ) {
                IloNumVar[] array = new IloNumVar[2 * _array.length];
                System.arraycopy(_array, 0, array, 0, _num);
                _array = array;
            }
            _array[_num++] = ivar;
        }

        IloNumVar getElement(int i) { return _array[i]; }
        int       getSize()         { return _num; }
    }

    static void report1(IloCplex cutSolver, IloNumVarArray Cut, IloRange[] Fill)
            throws IloException {
        System.out.println();
        //输出共切了多少个17英尺木材
        System.out.println("Using " + cutSolver.getObjValue() + " rolls");
        System.out.println();
        for (int j = 0; j < Cut.getSize(); j++) {
            //输出每种切法用了多少木材
            System.out.println("  Cut" + j + " = " +
                    cutSolver.getValue(Cut.getElement(j)));
        }
        System.out.println();
        for (int i = 0; i < Fill.length; i++) {
            //输出每种木材所需的切法数除以个数
            System.out.println("  Fill" + i + " = " + cutSolver.getDual(Fill[i]));
            System.out.println(Fill[i]);
        }
        System.out.println();
    }
    static void report2(IloCplex patSolver, IloNumVar[] Use)
            throws IloException {
        System.out.println();
        System.out.println("Reduced cost is " + patSolver.getObjValue());
        System.out.println();
        if (patSolver.getObjValue() <= -RC_EPS) {
            for (int i = 0; i < Use.length; i++)
                System.out.println("  Use" + i + " = "
                        + patSolver.getValue(Use[i]));
            System.out.println();
        }
    }
    static void report3(IloCplex cutSolver, IloNumVarArray Cut)
            throws IloException {
        System.out.println();
        System.out.println("Best integer solution uses " +
                cutSolver.getObjValue() + " rolls");
        System.out.println();
        for (int j = 0; j < Cut.getSize(); j++)
            System.out.println("  Cut" + j + " = " +
                    cutSolver.getValue(Cut.getElement(j)));
    }
}
