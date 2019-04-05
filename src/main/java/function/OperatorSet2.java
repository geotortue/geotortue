/**
 * 
 */
package function;

import java.util.Stack;

import org.nfunk.jep.ASTFunNode;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.EvaluatorI;
import org.nfunk.jep.Node;
import org.nfunk.jep.Operator;
import org.nfunk.jep.OperatorSet;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.Variable;
import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPException.JEPTrouble;
import org.nfunk.jep.function.CallbackEvaluationI;
import org.nfunk.jep.function.LValueI;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;
import type.JAssignment;
import type.JBoolean;
import type.JIterable;
import type.JList;
import type.JMethod;
import type.JNumber;
import type.JObjectHelper;
import type.JObjectI;
import type.JObjectI.JEP2Type;
import type.JObjectsVector;
import type.JSlicer;

/**
 * @author Salvatore Tummarello
 *
 */
public class OperatorSet2 extends OperatorSet {

	public OperatorSet2() {
		this.OP_GT = new Operator(">", new Comparative2(Comparative2.ID.GT));
		this.OP_LT = new Operator("<", new Comparative2(Comparative2.ID.LT));
		this.OP_EQ = new Operator("==", new Comparative2(Comparative2.ID.EQ));
		this.OP_LE = new Operator("<=", new Comparative2(Comparative2.ID.LE));
		this.OP_GE = new Operator(">=", new Comparative2(Comparative2.ID.GE));
		this.OP_NE = new Operator("!=", new Comparative2(Comparative2.ID.NE));

		this.OP_AND = new Operator("&&", new Logical2(Logical2.ID.AND));
		this.OP_OR = new Operator("||", new Logical2(Logical2.ID.OR));
		this.OP_NOT = new Operator("!", new Not2());

		this.OP_ADD = new Operator("+", new Add2());
		this.OP_SUBTRACT = new Operator("-", new Subtract2());
		this.OP_UMINUS = new Operator("UMinus", "-", new UMinus2());

		this.OP_MULTIPLY = new Operator("*", new Multiply2());
		this.OP_DIVIDE = new Operator("/", new Divide2());
		this.OP_MOD = new Operator("%", new Mod2());

		this.OP_POWER = new Operator("^", new Power2());

		this.OP_LIST = new Operator("LIST", new List2());
		this.OP_ELEMENT = new Operator("[]", new Ele2());

		this.OP_ASSIGN = new Operator("=", new Assign2());

		this.OP_DOT = new Operator(".", new Dot2()); // get a method operator
		this.OP_CROSS = new Operator(":", new Slice2()); // workaround to get a slice operator
	}

	public static class Comparative2 extends PostfixMathCommand2 {
		private static enum ID {
			LT, GT, LE, GE, NE, EQ
		};

		private final ID id;

		public Comparative2(ID id) {
			super(2);
			this.id = id;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> b = popJEPObjectI(inStack);
			JObjectI<?> a = popJEPObjectI(inStack);

			int comp = JObjectHelper.compare(a, b);

			boolean res = false;
			switch (id) {
			case LT:
				res = (comp < 0);
				break;
			case GT:
				res = (comp > 0);
				break;
			case LE:
				res = (comp <= 0);
				break;
			case GE:
				res = (comp >= 0);
				break;
			case NE:
				res = (comp != 0);
				break;
			case EQ:
				res = (comp == 0);
				break;
			}

			return res ? JBoolean.TRUE : JBoolean.FALSE;
		}
	}

	public static class Logical2 extends PostfixMathCommand2 {
		private static enum ID {
			AND, OR
		};

		private final ID id;

		public Logical2(ID id) {
			super(2);
			this.id = id;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> b = popJEPObjectI(inStack);
			JObjectI<?> a = popJEPObjectI(inStack);

			boolean b1;
			if (a.isANumber())
				b1 = ((JNumber<?>) a).doubleValue() != 0;
			else if (a.isIterable())
				b1 = !((JIterable) a).getItems().isEmpty();
			else
				b1 = ((JBoolean) a).getValue();

			boolean b2;
			if (b.isANumber())
				b2 = ((JNumber<?>) b).doubleValue() != 0;
			else if (b.isIterable())
				b2 = !((JIterable) b).getItems().isEmpty();
			else
				b2 = ((JBoolean) b).getValue();

			boolean res = false;
			switch (id) {
			case AND:
				res = b1 && b2;
				break;
			case OR:
				res = b1 || b2;
				break;
			}

			return res ? JBoolean.TRUE : JBoolean.FALSE;
		}
	}

	private class Not2 extends PostfixMathCommand2 {

		public Not2() {
			super(1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> a = popJEPObjectI(inStack);

			boolean b;
			if (a.isANumber())
				b = ((JNumber<?>) a).doubleValue() == 0;
			else if (a.isIterable())
				b = ((JIterable) a).getItems().isEmpty();
			else
				b = !((JBoolean) a).getValue();

			return b ? JBoolean.TRUE : JBoolean.FALSE;
		}
	}

	private class Add2 extends PostfixMathCommand2 {

		public Add2() {
			super(-1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> sum = popJEPObjectI(inStack);

			int i = 1;
			while (i < curNumberOfParameters) {
				JObjectI<?> o = popJEPObjectI(inStack);
				sum = o.add(sum);
				i++;
			}

			return sum;
		}
	}

	private class Subtract2 extends PostfixMathCommand2 {

		public Subtract2() {
			super(2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JNumber<?> y = popNumber(inStack);
			JNumber<?> x = popNumber(inStack);
			return x.sub(y);
		}
	}

	private class UMinus2 extends PostfixMathCommand2 {

		public UMinus2() {
			super(1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JNumber<?> a = popNumber(inStack);
			return a.opp();
		}
	}

	private class Multiply2 extends PostfixMathCommand2 {

		public Multiply2() {
			super(-1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> prod = popJEPObjectI(inStack);

			int i = 1;
			while (i < curNumberOfParameters) {
				JObjectI<?> o = popJEPObjectI(inStack);
				prod = o.mul(prod);
				i++;
			}

			return prod;
		}
	}

	private class Divide2 extends PostfixMathCommand2 {

		public Divide2() {
			super(2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JNumber<?> y = popNumber(inStack);
			JNumber<?> x = popNumber(inStack);
			if (y.isZero())
				throw new JEPException(JEP2Trouble.JEP2_DIV_BY_0);
			return x.div(y);
		}
	}

	private class Mod2 extends PostfixMathCommand2 {

		public Mod2() {
			super(2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JNumber<?> y = popNumber(inStack);
			JNumber<?> x = popNumber(inStack);
			if (y.isZero())
				throw new JEPException(JEP2Trouble.JEP2_DIV_BY_0);
			return x.mod(y);
		}
	}

	private class Power2 extends PostfixMathCommand2 {

		public Power2() {
			super(2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JNumber<?> y = popNumber(inStack);
			JNumber<?> x = popNumber(inStack);
			if (x.isZero() && y.isZero())
				throw new JEPException(JEP2Trouble.JEP2_0_POW_0);
			if (y.isZero())
				return JEP2.createNumber(1L);
			return x.pow(y);
		}
	}

	private class List2 extends PostfixMathCommand2 {
		public List2() {
			super(-1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectsVector v = new JObjectsVector();
			v.setSize(curNumberOfParameters);
			for (int i = curNumberOfParameters - 1; i >= 0; --i) {
				JObjectI<?> o = popJEPObjectI(inStack);
				if (o.getType() == JEP2Type.NULL)
					return JEP2.createList(new JObjectsVector());
				v.setElementAt(o, i);
			}
			return JEP2.createList(v);
		}

	}

	private class Ele2 extends PostfixMathCommand2 implements LValueI {

		public Ele2() {
			super(2);
		}

		public void set(EvaluatorI pv, Node node, Object value) throws ParseException {
			Node lhsNode = node.jjtGetChild(0);

			if (!(lhsNode instanceof ASTVarNode))
				throw new ParseException("Node is not a VarNode"); 

			ASTVarNode vn = (ASTVarNode) lhsNode;
			Variable var = vn.getVar();

			JIterable list;
			JList idx;
			try {
				JObjectI<?> o = (JObjectI<?>) var.getValue();
				if (o == null)
					throw new JEPException(JEPTrouble.JEP_UNRECOGNIZED_VARIABLE, var.getName());
				list = JEP2.getIterable(o);
				idx = (JList) pv.eval(node.jjtGetChild(1));
			} catch (ClassCastException ex) {
				ex.printStackTrace(); 
				return;
			}

			JObjectI<?> v;
			if (value instanceof JObjectI<?>)
				v = (JObjectI<?>) value;
			else if (value instanceof String)
				v = JEP2.createString((String) value);
			else
				throw new ParseException(value+" of class "+value.getClass()+" is not a JEP2Object");
			JObjectI<?> newVarVal = list.changeElementAt(idx, v);
			var.setValue(newVarVal);

		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> rhs = popJEPObjectI(inStack);
			JObjectI<?> lhs = popJEPObjectI(inStack);
			
			JIterable it = JEP2.getIterable(lhs);
			return it.elementAt(JEP2.getList(rhs));
		}

	}

	private class Assign2 extends PostfixMathCommand2 implements CallbackEvaluationI {

		public Assign2() {
			super(2);
		}

		public Object evaluate(Node node, EvaluatorI pv) throws ParseException {
			if (node.jjtGetNumChildren() != 2)
				throw new ParseException("Node should have 2 children");

			// evaluate the value of the righthand side.
			Object rhsVal = pv.eval(node.jjtGetChild(1));
			if (rhsVal instanceof String)
				rhsVal = JEP2.createString((String) rhsVal);

			// Set the value of the variable on the lhs.
			Node lhsNode = node.jjtGetChild(0);
			if (lhsNode instanceof ASTVarNode) {
				ASTVarNode vn = (ASTVarNode) lhsNode;
				Variable var = vn.getVar();
				if (var.isConstant())
					throw new JEPException(JEP2Trouble.JEP2_CANNOT_CHANGE_A_CONSTANT, var.getName());
				var.setValue(rhsVal);
				return new JAssignment(var.getName(), rhsVal);
			} else if (lhsNode instanceof ASTFunNode && ((ASTFunNode) lhsNode).getPFMC() instanceof LValueI) {
				((LValueI) ((ASTFunNode) lhsNode).getPFMC()).set(pv, lhsNode, rhsVal);
				Node varNode= lhsNode.jjtGetChild(0);
				if (!(varNode instanceof ASTVarNode))
					throw new ParseException("Node is not a VarNode"); 
				ASTVarNode var = (ASTVarNode) varNode;
				return new JAssignment(var.getName(), var.getVar().getValue());
			}
			throw new ParseException("Invalid Node");
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			return null;
		}
	}

	private class Dot2 extends PostfixMathCommand2 {

		public Dot2() {
			super(2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			JObjectI<?> list = popJEPObjectI(inStack);
			
			if (o.getType() != JEP2Type.METHOD)
				throw new JEPException(JEP2Trouble.JEP2_NOT_A_METHOD, o.toString());
			
			return ((JMethod) o).getResult(list);
		}
	}
	
	private class Slice2 extends PostfixMathCommand2 {

		public Slice2() {
			super(2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> y = popJEPObjectI(inStack);
			JObjectI<?> x = popJEPObjectI(inStack);
			return new JSlicer(x, y);
		}
	}
}