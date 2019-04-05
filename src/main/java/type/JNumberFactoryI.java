/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.type.Complex;
import org.nfunk.jep.type.NumberFactory;

/**
 * @author Salvatore Tummarello
 *
 */
public interface JNumberFactoryI extends NumberFactory {

	@Override
	public JNumber<?> createNumber(String value) throws JEPException ;

	@Override
	public JNumber<?> createNumber(double value);

	@Override
	public JInteger createNumber(int value);

	@Override
	public JInteger createNumber(short value);

	@Override
	public JNumber<?> createNumber(float value);

	@Override
	public JInteger createNumber(boolean value);

	@Override
	public JNumber<?> createNumber(Number value);

	@Override
	public JObjectI<?> createNumber(Complex value);

	@Override
	public JInteger getZero();

	@Override
	public JInteger getOne();

	@Override
	public JInteger getMinusOne();

	@Override
	public JInteger getTwo();
	
	public JList createList(JObjectsVector v);
	
	public JString createString(String s);
	
	public JBoolean createBoolean(boolean b);
	
	public JDict createDict(JHashTable t);
}
