package main.java.micrograd.engine;
import main.java.utils.CollectionUtil;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class Value {
    public double data;
    public double grad;
    IBackward backward = null;
    Set<Value> _prev;
    String _op;
    public Value(double data) {
        this(data, Collections.EMPTY_LIST, "");
    }

    public Value(double data, List <Value> _children) {
        this(data, _children, "");
    }

    public Value(double data, List <Value> _children, String _op) {
        this.data = data;
        this.grad = 0;
        this._prev = CollectionUtil.newHashset(_children);
        this._op = _op;
    }

    public Value add(Value OtherValue) {
        List <Value> _children = CollectionUtil.newArrayList();
        _children.add(this);
        _children.add(OtherValue);
        Value OutValue = new Value(this.data + OtherValue.data, _children, "+");
        class Backward implements IBackward {
            @Override
            public void _backward() {
                grad += OutValue.grad;
                OtherValue.grad += OutValue.grad;
            }
        }
        OutValue.backward = new Backward();
        return OutValue;
    }

    public Value mul(Value OtherValue) {
        List <Value> _children = CollectionUtil.newArrayList();
        _children.add(this);
        _children.add(OtherValue);
        Value OutValue = new Value(this.data * OtherValue.data, _children, "*");
        class Backward implements IBackward {
            @Override
            public void _backward() {
                grad += OtherValue.data * OutValue.grad;
                OtherValue.grad += data * OutValue.grad;
            }
        }
        OutValue.backward = new Backward();
        return OutValue;
    }
    public Value pow(double OtherValue) {
        List<Value> _children = CollectionUtil.newArrayList();
        _children.add(this);
        Value outValue = new Value(Math.pow(this.data, OtherValue), _children, "**");
        class Backward implements IBackward {
            @Override
            public void _backward() {
                grad += (OtherValue * Math.pow(data, (OtherValue - 1))) * outValue.grad;
            }
        }
        outValue.backward = new Backward();
        return outValue;
    }
    public Value relu() {
        List<Value> _children = CollectionUtil.newArrayList();
        _children.add(this);
        Value outValue;
        if(this.data < 0) {
            outValue = new Value(0, _children, "ReLu");
        } else {
            outValue = new Value(this.data, _children, "ReLu");
        }
        class Backward implements IBackward {
            @Override
            public void _backward() {
                if(outValue.data > 0) {
                    grad += 1 * outValue.grad;
                } else {
                    grad += 0 * outValue.grad;
                }
            }
        }
        outValue.backward = new Backward();
        return outValue;
    }
    public void buildTopo(Value value, List<Value> topoList, Set<Value> visitedSet) {
        if(!visitedSet.contains(value)) {
            visitedSet.add(value);
            for(Value child : value._prev) {
                buildTopo(child, topoList, visitedSet);
            }
            topoList.add(value);
        }
    }
    public void backward() {
        // topological order all the children in the graph
        List<Value> topoList = CollectionUtil.newArrayList();
        Set<Value> visitedSet = CollectionUtil.newHashset();
        buildTopo(this, topoList, visitedSet);
        // go one variable at a time and apple the chain rule to get its gradient
        this.grad = 1;
        Collections.reverse(topoList);
        for(Value value : topoList) {
            if(null != value.backward) {
                value.backward._backward();
            }
        }
    }
    public Value neg(){
        return this.mul(new Value(-1));
    }
    public Value add(double other) {
        return this.add(new Value(other));
    }
    public Value sub(double other) {
        other = other * -1;
        return this.add(new Value(other));
    }
    public Value mul(double other) {
        return this.mul(new Value(other));
    }
    public Value div(double other) {
        return this.mul(new Value(other).pow(-1));
    }
    public Value rdiv(double other) {
        return new Value(other).mul(this.pow(-1));
    }

    @Override
    public String toString() {
        return "Value(data={"+ this.data +"}, grad={" +this.grad + "})";
    }
}
