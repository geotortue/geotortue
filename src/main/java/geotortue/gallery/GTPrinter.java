package geotortue.gallery;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.DialogTypeSelection;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

public class GTPrinter {

	public static void print(final BufferedImage img) {
		if (img==null)
			return;
		
		
		final PrinterJob job = PrinterJob.getPrinterJob();

		final HashPrintRequestAttributeSet printRequestSet = new HashPrintRequestAttributeSet();
		printRequestSet.add(MediaSizeName.ISO_A4);
		printRequestSet.add(DialogTypeSelection.COMMON);
		printRequestSet.add(OrientationRequested.PORTRAIT);
		
		job.setPrintable(new GTPrinterJob(job, img));
		
		if (job.printDialog(printRequestSet)) {
			new Thread() {
				@Override
				public void run() {
					try {
						job.print(printRequestSet);
					} catch (PrinterException ex) {
						ex.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	private static class GTPrinterJob implements Printable {
		private  BufferedImage img;

		public GTPrinterJob(PrinterJob printJob, BufferedImage i) {
			this.img = i;
		}

		@Override
		public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
			switch (pageIndex) {
			case 0: {
				int x = (int) pageFormat.getImageableX();
				int y = (int) pageFormat.getImageableY();

				Graphics2D g2d = (Graphics2D) g;

				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				double width = pageFormat.getImageableWidth();
				double height = pageFormat.getImageableHeight();
				System.out.println(x+" "+y+" "+width+" "+height);
				int iw = img.getWidth();
				int ih = img.getHeight();

				double wratio = iw / width ;
				double hratio = ih / height;
				double ratio = Math.min(wratio, hratio);
				if (ratio>1) ratio = 1;
				int w = (int) (iw * ratio);
				int h = (int) (ih * ratio);

				g2d.drawImage(img, x, y, w, h, null);

				g2d.setColor(Color.GRAY);
				g2d.drawRect(x, y, w, h);

				return Printable.PAGE_EXISTS;
			}
			default:
				return Printable.NO_SUCH_PAGE;
			}
		}
		
	}
}
