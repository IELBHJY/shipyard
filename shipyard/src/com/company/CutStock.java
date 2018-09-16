/* --------------------------------------------------------------------------
 * File: CutStock.java
 * Version 12.8.0
 * --------------------------------------------------------------------------
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
 * Copyright IBM Corporation 2001, 2017. All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 * --------------------------------------------------------------------------
 */
package com.company;
import ilog.concert.*;
import ilog.cplex.*;
import java.io.*;

class CutStock {
    static double RC_EPS = 1.0e-6;

    // Data of the problem
    static double   _rollWidth;
    static double[] _size;//需求尺寸
    static double[] _amount;//需求数量

    static void readData(String fileName) throws IOException{//,InputDataReader.InputDataReaderException {
        //InputDataReader reader = new InputDataReader(fileName);
        //_rollWidth = reader.readDouble();
       // _size      = reader.readDoubleArray();
       // _amount    = reader.readDoubleArray();
    }
    //输出共切了多少根17英尺的木材以及每种切法用了多少木材
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
        for (int i = 0; i < Fill.length; i++)
            //输出每种木材所需的切法数除以个数
            System.out.println("  Fill" + i + " = " + cutSolver.getDual(Fill[i]));
        System.out.println();
    }

    //输出影子价格以及新的切法
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

    public static void main( String[] args ) throws IloException {
        String datafile = "/Applications/CPLEX_Studio128/cplex/examples/data/cutstock.dat";
        try {
            if (args.length > 0)
                datafile = args[0];
            //readData(datafile);
            _rollWidth=20;
            _size=new double[]{3,5,9};
            _amount=new double[]{25,20,23,12,13,12,11};
            /// CUTTING-OPTIMIZATION PROBLEM ///
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
            /*for (int j = 0; j < nWdth; j++)
                Cut.add(cutSolver.numVar(cutSolver.column(RollsUsed, 1.0).and(
                        cutSolver.column(Fill[j], (int)(_rollWidth/_size[j]))
                        ),
                        0.0, Double.MAX_VALUE));//添加一个变量x1。*/
            for(int i=0;i<5;i++){
                IloColumn column = cutSolver.column(RollsUsed, 1.);
                for ( int p = 0; p <7; p++ )
                    column = column.and(cutSolver.column(Fill[p], (p+1)*2));
                Cut.add( cutSolver.numVar(column, 0., Double.MAX_VALUE) );
            }
            cutSolver.setParam(IloCplex.Param.RootAlgorithm, IloCplex.Algorithm.Primal);

            /// PATTERN-GENERATION PROBLEM ///
            IloCplex patSolver = new IloCplex();
            patSolver.setOut(null);
            IloObjective ReducedCost = patSolver.addMinimize();
            IloNumVar[] Use = patSolver.numVarArray(nWdth,
                    0., Double.MAX_VALUE,
                    IloNumVarType.Int);// a3  a5  a9
            patSolver.addRange(-Double.MAX_VALUE,
                    patSolver.scalProd(_size, Use),
                    _rollWidth);//添加约束

            /// COLUMN-GENERATION PROCEDURE ///

            double[] newPatt = new double[nWdth];

            /// COLUMN-GENERATION PROCEDURE ///

            for (;;) {
                /// OPTIMIZE OVER CURRENT PATTERNS ///
                cutSolver.solve();
                report1(cutSolver, Cut, Fill);
                /// FIND AND ADD A NEW PATTERN ///
                double[] price = cutSolver.getDuals(Fill);//获取影子价格
                ReducedCost.setExpr(patSolver.diff(1.,
                        patSolver.scalProd(Use, price)));//更新目标函数
                patSolver.solve();
                report2 (patSolver, Use);
                if ( patSolver.getObjValue() > -RC_EPS )
                    break;
                newPatt = patSolver.getValues(Use);//更新新生成的列
                IloColumn column = cutSolver.column(RollsUsed, 1.);
                for ( int p = 0; p < newPatt.length; p++ )
                    column = column.and(cutSolver.column(Fill[p], newPatt[p]));
                Cut.add( cutSolver.numVar(column, 0., Double.MAX_VALUE) );
            }
            report3(cutSolver,Cut);
            //将cutSolver转换成整数规划，然后再求解。
            for ( int i = 0; i < Cut.getSize(); i++ ) {
                cutSolver.add(cutSolver.conversion(Cut.getElement(i),
                        IloNumVarType.Int));
            }
            cutSolver.solve();
            report3 (cutSolver, Cut);
            System.out.println("Solution status: " + cutSolver.getStatus());
            cutSolver.end();
            patSolver.end();
        }
        catch ( IloException exc ) {
            System.err.println("Concert exception '" + exc + "' caught");
        }
    }
}


/* Example Input file:
115
[25, 40, 50, 55, 70]
[50, 36, 24, 8, 30]
*/

