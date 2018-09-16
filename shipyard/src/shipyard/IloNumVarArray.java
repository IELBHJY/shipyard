package shipyard;

import ilog.concert.IloNumVar;

public class IloNumVarArray {
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
