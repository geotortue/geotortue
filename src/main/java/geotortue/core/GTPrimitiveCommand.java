package geotortue.core;

import org.nfunk.jep.addon.JEPException;

import fw.HelpI;
import geotortue.core.GTCommandFactory.GTCommandKey;
import geotortue.core.GTCommandProcessor.GTInterruptionException;
import geotortue.core.GTMessageFactory.GTTrouble;
import jep2.JEP2;
import jep2.JEP2Exception;
import type.JObjectI;

public abstract class GTPrimitiveCommand implements HelpI {
	
	public final GTCommandKey key;
	private final int arity;
	private final String description;
	
	public GTPrimitiveCommand(GTCommandKey key) {
		this(key, -1);
	}
	
	public GTPrimitiveCommand(GTCommandKey key, int arity) {
		this.key = key;
		this.arity = arity;
		this.description = GTCommandDescTable.getDescription(this);
	}
	
	public GTPrimitiveCommand(GTPrimitiveCommand c, GTCommandKey key) {
		this(key, c.arity);
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	public JObjectI<?> execute(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException {
		int n = bundle.getArgumentsCount();
		if (arity>=0 && n!=arity) {
			String name = GTCommandDescTable.getName(key);
			throw new GTException(this, GTTrouble.GTJEP_ARITY, bundle, name, arity+"");
		}
		try {
			return execute_(bundle, context);
		} catch (JEP2Exception ex) {
			throw new GTException(ex, bundle);
		} catch (JEPException e) {
			throw new GTException(new JEP2Exception(this, e), bundle);
		}
	}
	
	protected abstract JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException;
	
	protected double getDoubleAt(GTProcessingContext context, GTCommandBundle bundle, int pos) throws GTException, GTInterruptionException, JEPException {
		JObjectI<?> o = context.getJObjectAt(bundle, pos);
		return JEP2.getDouble(o);
	}
	
	protected boolean getBooleanAt(GTProcessingContext context, GTCommandBundle bundle, int pos) throws GTException, GTInterruptionException, JEPException {
		JObjectI<?> o = context.getJObjectAt(bundle, pos);
		return JEP2.getBoolean(o);
	}

	protected String getStringSince(GTProcessingContext context, GTCommandBundle bundle, int pos) throws GTException, GTInterruptionException, JEPException {
		SourceLocalization str = bundle.getLocalizationSince(pos);
		JObjectI<?> o = context.getJObject(str);
		return JEP2.getString(o).getValue();
		
	}
}