/*
 * Copyright (C) 2003 Idega software. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega software. Use is
 * subject to license terms.
 *  
 */
package se.idega.idegaweb.commune.accounting.export.ifs.business;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import se.idega.idegaweb.commune.accounting.business.AccountingUtil;
import se.idega.idegaweb.commune.accounting.business.PaymentComparator;
import se.idega.idegaweb.commune.accounting.export.data.ExportDataMapping;
import se.idega.idegaweb.commune.accounting.export.ifs.data.IFSCheckHeader;
import se.idega.idegaweb.commune.accounting.export.ifs.data.IFSCheckHeaderHome;
import se.idega.idegaweb.commune.accounting.export.ifs.data.IFSCheckRecord;
import se.idega.idegaweb.commune.accounting.export.ifs.data.IFSCheckRecordHome;
import se.idega.idegaweb.commune.accounting.export.ifs.data.JournalLog;
import se.idega.idegaweb.commune.accounting.export.ifs.data.JournalLogHome;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceHeader;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceHeaderHome;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecord;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecordHome;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentHeader;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentHeaderHome;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecord;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecordHome;
import se.idega.idegaweb.commune.accounting.posting.business.PostingBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingException;
import se.idega.idegaweb.commune.accounting.school.business.ProviderBusiness;
import se.idega.idegaweb.commune.accounting.school.business.StudyPathException;
import se.idega.idegaweb.commune.accounting.school.data.Provider;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.data.IDOException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.user.data.User;
import com.idega.util.CalendarMonth;
import com.idega.util.IWTimestamp;

/**
 * @author palli
 */
public class IFSFileCreationThread extends Thread {
	protected String _schoolCategory = null;
	protected IWTimestamp _paymentDate = null;
	protected String _periodText = null;
	protected User _user = null;
	protected Locale _currentLocale = null;
	protected IWApplicationContext _iwac = null;
	private HSSFWorkbook wb = null;
	private HSSFRow row = null;
	private HSSFCell cell = null;
	private HSSFCellStyle styleAlignRight = null;
	private HSSFCellStyle styleBold = null;
	private HSSFCellStyle styleBoldAlignRight = null;
	private HSSFCellStyle styleBoldUnderline = null;
	private HSSFCellStyle styleBoldUnderlineAlignRight = null;
	private HSSFCellStyle styleItalicUnderlineAlignRight = null;
	private float inCommuneSum = 0;
	private NumberFormat numberFormat = null;
	private String deviationString = "";

	private final static String IW_BUNDLE_IDENTIFIER = "se.idega.idegaweb.commune.accounting";

	/*
	 * private final static String FILE_TYPE_PREFIX = "n24_ifs"; private final
	 * static String FILE_TYPE_HVD = "hvd"; private final static String
	 * FILE_TYPE_KND = "knd"; private final static String FILE_TYPE_LEV = "lev";
	 * private final static String FILE_TYPE_CHILDCARE = "bom"; private final
	 * static String FILE_TYPE_SCHOOL = "gsk";
	 */

	public IFSFileCreationThread(String schoolCategory, IWTimestamp paymentDate, String periodText, User user, Locale currentLocale, IWApplicationContext iwac) {
		_schoolCategory = schoolCategory;
		_paymentDate = paymentDate;
		_periodText = periodText;
		_user = user;
		_currentLocale = currentLocale;
		_iwac = iwac;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		createNumberFormat();
		IWTimestamp now = IWTimestamp.RightNow();
		JournalLog log;
		try {
			log = ((JournalLogHome) IDOLookup.getHome(JournalLog.class)).create();
			log.setSchoolCategoryString(_schoolCategory);
			log.setEventFileCreated();
			log.setEventDate(now.getTimestamp());
			log.setUser(_user);
			log.store();
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}
		catch (CreateException e) {
			e.printStackTrace();
		}

		IFSCheckHeader header = null;
		try {
			header = getIFSBusiness().getIFSCheckHeaderBySchoolCategory(_schoolCategory);
		}
		catch (RemoteException e) {
		}

		if (header == null) {
			try {
				header = ((IFSCheckHeaderHome) IDOLookup.getHome(IFSCheckHeader.class)).create();
			}
			catch (IDOLookupException e1) {
				e1.printStackTrace();
			}
			catch (CreateException e1) {
				e1.printStackTrace();
			}
		}
		else {
			Collection col = null;
			try {
				col = getIFSBusiness().getIFSCheckRecordByHeaderId(((Integer) header.getPrimaryKey()).intValue());
			}
			catch (RemoteException e5) {
				e5.printStackTrace();
			}
			catch (EJBException e5) {
				e5.printStackTrace();
			}

			if (col != null && !col.isEmpty()) {
				Iterator it = col.iterator();
				while (it.hasNext()) {
					IFSCheckRecord rec = (IFSCheckRecord) it.next();
					try {
						rec.remove();
					}
					catch (EJBException e1) {
						e1.printStackTrace();
					}
					catch (RemoveException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		header.setSchoolCategoryString(_schoolCategory);
		header.setStatusFileCreated();
		header.setEventDate(now.getTimestamp());
		header.setEventStartTime(now.getTimestamp());
		header.setEventEndTime(null);
		header.store();

		// Get folder info from ExportMappingBean
		ExportDataMapping mapping = null;
		String folder = null;
		try {
			mapping = getIFSBusiness().getExportBusiness().getExportDataMapping(_schoolCategory);
			folder = mapping.getFileCreationFolder();
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}

		SchoolCategory childCare = null;
		try {
			childCare = getIFSBusiness().getSchoolBusiness().getCategoryChildcare();
		}
		catch (RemoteException e2) {
			e2.printStackTrace();
		}

		SchoolCategory school = null;
		try {
			school = getIFSBusiness().getSchoolBusiness().getCategoryElementarySchool();
		}
		catch (RemoteException e3) {
			e3.printStackTrace();
		}

		SchoolCategory highSchool = null;
		try {
			highSchool = getIFSBusiness().getSchoolBusiness().getCategoryHighSchool();
		}
		catch (RemoteException e4) {
			e4.printStackTrace();
		}

		if (childCare != null && folder != null && school != null && highSchool != null) {
			if (_schoolCategory.equals(childCare.getPrimaryKey())) {
				StringBuffer fileName1 = new StringBuffer(folder);
				fileName1.append("n24_ifs_hvd_bom_");
				StringBuffer fileName2 = new StringBuffer(folder);
				fileName2.append("n24_ifs_lev_bom_");
				StringBuffer fileName3 = new StringBuffer(folder);
				fileName3.append("n24_ifs_knd_bom_");
				StringBuffer fileName4 = new StringBuffer(folder);
				fileName4.append("n24_attestlista_lev_bom_");
				StringBuffer fileName5 = new StringBuffer(folder);
				fileName5.append("n24_attestlista_knd_bom_");
				StringBuffer fileName6 = new StringBuffer(folder);
				fileName6.append("n24_avvikelselista_knd_bom_");
				StringBuffer fileName7 = new StringBuffer(folder);
				fileName7.append("n24_kommun_bom_");
				fileName1.append(now.getDateString("yyMMdd_HHmm"));
				fileName2.append(now.getDateString("yyMMdd_HHmm"));
				fileName3.append(now.getDateString("yyMMdd_HHmm"));
				fileName4.append(now.getDateString("yyMMdd_HHmm"));
				fileName5.append(now.getDateString("yyMMdd_HHmm"));
				fileName6.append(now.getDateString("yyMMdd_HHmm"));
				fileName7.append(now.getDateString("yyMMdd_HHmm"));

				try {
					createPaymentFiles(fileName1.toString(), fileName2.toString(), fileName4.toString(), fileName7.toString(), _schoolCategory, now, _paymentDate);
				}
				catch (IOException e5) {
					e5.printStackTrace();
				}
				try {
					createInvoiceFiles(fileName3.toString(), fileName5.toString(), fileName6.toString(), _schoolCategory, now, _currentLocale, _periodText, header);
				}
				catch (IOException e6) {
					e6.printStackTrace();
				}
			}
			else if (_schoolCategory.equals(school.getPrimaryKey())) {
				StringBuffer fileName1 = new StringBuffer(folder);
				fileName1.append("n24_ifs_hvd_gsk_");
				StringBuffer fileName2 = new StringBuffer(folder);
				fileName2.append("n24_ifs_lev_gsk_");
				StringBuffer fileName4 = new StringBuffer(folder);
				fileName4.append("n24_attestlista_lev_gsk_");
				StringBuffer fileName7 = new StringBuffer(folder);
				fileName7.append("n24_kommun_gsk_");
				fileName1.append(now.getDateString("yyMMdd_HHmm"));
				fileName2.append(now.getDateString("yyMMdd_HHmm"));
				fileName4.append(now.getDateString("yyMMdd_HHmm"));
				fileName7.append(now.getDateString("yyMMdd_HHmm"));

				try {
					createPaymentFiles(fileName1.toString(), fileName2.toString(), fileName4.toString(), fileName7.toString(), _schoolCategory, now, _paymentDate);
				}
				catch (IOException e5) {
					e5.printStackTrace();
				}
			}
			else if (_schoolCategory.equals(highSchool.getPrimaryKey())) {
				StringBuffer fileName1 = new StringBuffer(folder);
				fileName1.append("n24_ifs_hvd_gym_");
				StringBuffer fileName2 = new StringBuffer(folder);
				fileName2.append("n24_ifs_lev_gym_");
				StringBuffer fileName4 = new StringBuffer(folder);
				fileName4.append("n24_attestlista_lev_gym_");
				StringBuffer fileName7 = new StringBuffer(folder);
				fileName7.append("n24_kommun_gym_");
				fileName1.append(now.getDateString("yyMMdd_HHmm"));
				fileName2.append(now.getDateString("yyMMdd_HHmm"));
				fileName4.append(now.getDateString("yyMMdd_HHmm"));
				fileName7.append(now.getDateString("yyMMdd_HHmm"));
				try {
					createPaymentFiles(fileName1.toString(), fileName2.toString(), fileName4.toString(), fileName7.toString(), _schoolCategory, now, _paymentDate);
				}
				catch (IOException e5) {
					e5.printStackTrace();
				}
			}
		}

		now = IWTimestamp.RightNow();
		header.setEventEndTime(now.getTimestamp());
		header.store();
	}

	private void createPaymentFiles(String fileName1, String fileName2, String fileName3, String fileName4, String schoolCategory, IWTimestamp executionDate, IWTimestamp paymentDate) throws IOException {
		String localizedSchoolCategoryName = _iwac.getApplication().getBundle(IW_BUNDLE_IDENTIFIER).getResourceBundle(_currentLocale).getLocalizedString("school_category." + _schoolCategory);
		Collection phInCommune = null;
		try {
			phInCommune = ((PaymentHeaderHome) IDOLookup.getHome(PaymentHeader.class)).findBySchoolCategoryStatusInCommuneWithCommunalManagement(schoolCategory, 'P');
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}

		Collection phOutsideCommune = null;
		try {
			phOutsideCommune = ((PaymentHeaderHome) IDOLookup.getHome(PaymentHeader.class)).findBySchoolCategoryStatusOutsideCommuneOrWithoutCommunalManagement(schoolCategory, 'P');
		}
		catch (IDOLookupException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}
		Collection phAll = new ArrayList();
		phAll.addAll(phOutsideCommune);
		phAll.addAll(phInCommune);
		try {
			createPaymentSigningFilesExcel(phInCommune, fileName4 + ".xls", "Utbetalningsattestlista " + localizedSchoolCategoryName + ", egna kommunala anordnare, " + executionDate.getDateString("yyyy-MM-dd"), false);
			createPaymentSigningFilesExcel(phAll, fileName3 + ".xls", "Utbetalningsattestlista " + localizedSchoolCategoryName + ", " + executionDate.getDateString("yyyy-MM-dd"), true);
		}
		catch (IOException e3) {
			e3.printStackTrace();
		}
		catch (FinderException e3) {
			e3.printStackTrace();
		}

		if (phInCommune != null && !phInCommune.isEmpty()) {
			Collection rec = null;
			try {
				rec = ((PaymentRecordHome) IDOLookup.getHome(PaymentRecord.class)).findByPaymentHeaders(phInCommune);
			}
			catch (IDOLookupException e2) {
				e2.printStackTrace();
			}
			catch (FinderException e2) {
				e2.printStackTrace();
			}

			try {
				createPaymentFilesExcel(rec, fileName1 + ".xls", "Checkutbetalning " + localizedSchoolCategoryName + ", egna kommunala anordnare, " + executionDate.getDateString("yyyy-MM-dd"), true);
			}
			catch (IOException e3) {
				e3.printStackTrace();
			}

			Iterator it = rec.iterator();
			FileWriter writer = null;
			try {
				writer = new FileWriter(fileName1);
			}
			catch (IOException e4) {
				e4.printStackTrace();
			}

			BufferedWriter bWriter = new BufferedWriter(writer);

			PostingBusiness pb = getIFSBusiness().getPostingBusiness();

			while (it.hasNext()) {
				PaymentRecord pRec = (PaymentRecord) it.next();
				if (pRec.getTotalAmount() != 0.0f) {
					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyyy"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Konto"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Ansvar"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Resurs"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Verksamhet"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Aktivitet"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Projekt"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Objekt"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getOwnPosting(), "Motpart"));
					bWriter.write(";");
					//anlaggningsnummer
					bWriter.write(";");
					//internranta
					bWriter.write(";");
					bWriter.write("SEK");
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(Integer.toString(Math.round(pRec.getTotalAmount())));
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(Integer.toString(Math.round(pRec.getTotalAmount())));
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyMM") + " " + pRec.getPaymentText());
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(paymentDate.getDateString("yyyy-MM-dd"));
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.newLine();

					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyyy"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Konto"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Ansvar"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Resurs"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Verksamhet"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Aktivitet"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Projekt"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Objekt"));
					bWriter.write(";");
					bWriter.write(pb.findFieldInStringByName(pRec.getDoublePosting(), "Motpart"));
					bWriter.write(";");
					//anlaggningsnummer
					bWriter.write(";");
					//internranta
					bWriter.write(";");
					bWriter.write("SEK");
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(Integer.toString(Math.round(-pRec.getTotalAmount())));
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(Integer.toString(Math.round(-pRec.getTotalAmount())));
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyMM") + " " + pRec.getPaymentText());
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.write(paymentDate.getDateString("yyyy-MM-dd"));
					bWriter.write(";");
					//empty
					bWriter.write(";");
					//empty
					bWriter.write(";");
					bWriter.newLine();
				}

				pRec.setStatus('L');
				pRec.store();
			}

			bWriter.close();

			Iterator itor = phInCommune.iterator();
			while (itor.hasNext()) {
				PaymentHeader head = (PaymentHeader) itor.next();
				head.setStatus('L');
				head.store();
			}
		}

		if (phOutsideCommune != null && !phOutsideCommune.isEmpty()) {
			NumberFormat format = NumberFormat.getInstance(_currentLocale);
			format.setMaximumFractionDigits(2);
			format.setMinimumFractionDigits(2);
			format.setMinimumIntegerDigits(1);
			format.setGroupingUsed(false);
			//			format.
			Collection recOutside = null;
			try {
				recOutside = ((PaymentRecordHome) IDOLookup.getHome(PaymentRecord.class)).findByPaymentHeaders(phOutsideCommune);
			}
			catch (IDOLookupException e2) {
				e2.printStackTrace();
			}
			catch (FinderException e2) {
				e2.printStackTrace();
			}
			try {
				createPaymentFilesExcel(recOutside, fileName2 + ".xls", "Checkutbetalning " + localizedSchoolCategoryName + ", �vriga anordnare, " + executionDate.getDateString("yyyy-MM-dd"), false);
			}
			catch (IOException e3) {
				e3.printStackTrace();
			}
			catch (Exception e3) {
				e3.printStackTrace();
			}

			FileWriter writer = new FileWriter(fileName2);
			BufferedWriter bWriter = new BufferedWriter(writer);

			PostingBusiness pb = getIFSBusiness().getPostingBusiness();
			ProviderBusiness provBiz = getIFSBusiness().getProviderBusiness();

			Iterator phIt = phOutsideCommune.iterator();
			while (phIt.hasNext()) {
				PaymentHeader pHead = (PaymentHeader) phIt.next();
				Provider prov = null;
				try {
					prov = provBiz.getProvider(pHead.getSchoolID());
				}
				catch (StudyPathException e4) {
					e4.printStackTrace();
				}
				catch (RemoteException e4) {
					e4.printStackTrace();
				}
				Collection rec = null;
				try {
					rec = ((PaymentRecordHome) IDOLookup.getHome(PaymentRecord.class)).findByPaymentHeader(pHead);
				}
				catch (IDOLookupException e3) {
					e3.printStackTrace();
				}
				catch (FinderException e3) {
					e3.printStackTrace();
				}
				if (rec != null && !rec.isEmpty()) {
					Iterator prIt = rec.iterator();
					Iterator sumIt = rec.iterator();

					float sum = 0;
					while (sumIt.hasNext()) {
						PaymentRecord r = (PaymentRecord) sumIt.next();
						sum += AccountingUtil.roundAmount(r.getTotalAmount());
					}

					String giro = prov.getAccountingProperties().getBankgiro();
					if (giro == null)
						giro = prov.getAccountingProperties().getPostgiro();
					if (giro == null)
						giro = "";

					bWriter.write("H");
					bWriter.write(";");
					bWriter.write(giro);
					bWriter.write(";");
					bWriter.write(((Integer) pHead.getPrimaryKey()).toString());
					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyyy-MM-dd"));
					bWriter.write(";");
					bWriter.write("SUPPEXT");
					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyyy-MM-dd"));
					bWriter.write(";");
					bWriter.write(executionDate.getDateString("yyyy-MM-dd"));
					bWriter.write(";");
					bWriter.write(paymentDate.getDateString("yyyy-MM-dd"));
					bWriter.write(";");
					bWriter.write(Integer.toString(IWTimestamp.getDaysBetween(executionDate, paymentDate) + 1));
					bWriter.write(";");
					bWriter.write("SEK");
					bWriter.write(";");
					//empty
					bWriter.write("-");
					bWriter.write(";");
					bWriter.write("*");
					bWriter.write(";");
					for (int i = 13; i < 55; i++)
						bWriter.write("-;");
					bWriter.write("-");
					bWriter.newLine();

					bWriter.write("I");
					bWriter.write(";");
					bWriter.write(giro);
					bWriter.write(";");
					bWriter.write(((Integer) pHead.getPrimaryKey()).toString());
					bWriter.write(";");
					bWriter.write("1");
					bWriter.write(";");
					//VAT code, changed L6 to 11...
					bWriter.write("11");
					bWriter.write(";");
					bWriter.write(format.format(sum));
					bWriter.write(";");
					bWriter.write(format.format(sum));
					bWriter.write(";");
					bWriter.write("0,00");
					bWriter.write(";");
					bWriter.write("0,00");
					bWriter.write(";");
					for (int i = 10; i < 23; i++)
						bWriter.write("-;");
					bWriter.write("-");
					bWriter.newLine();

					int row = 1;
					while (prIt.hasNext()) {
						PaymentRecord pRec = (PaymentRecord) prIt.next();

						if (pRec.getTotalAmount() != 0.0f) {
							bWriter.write("P");
							bWriter.write(";");
							bWriter.write(giro);
							bWriter.write(";");
							bWriter.write(((Integer) pHead.getPrimaryKey()).toString());
							bWriter.write(";");
							bWriter.write("1");
							bWriter.write(";");
							bWriter.write(Integer.toString(row));
							row++;
							bWriter.write(";");
							String tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Konto");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Ansvar");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Resurs");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Verksamhet");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Aktivitet");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Projekt");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Objekt");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							tmp = pb.findFieldInStringByName(pRec.getOwnPosting(), "Motpart");
							if ("".equals(tmp))
								bWriter.write("-");
							else
								bWriter.write(tmp);
							bWriter.write(";");
							//anlaggningsnummer
							bWriter.write("-");
							bWriter.write(";");
							//internranta
							bWriter.write("-");
							bWriter.write(";");
							//empty
							bWriter.write("-");
							bWriter.write(";");
							bWriter.write(format.format(AccountingUtil.roundAmount(pRec.getTotalAmount())));
							bWriter.write(";");
							bWriter.write(format.format(AccountingUtil.roundAmount(pRec.getTotalAmount())));
							bWriter.write(";");
							//empty
							bWriter.write("-");
							bWriter.write(";");
							//empty
							bWriter.write("-");
							bWriter.write(";");
							bWriter.write(pRec.getPaymentText());
							bWriter.write(";");
							bWriter.write("-");
							bWriter.newLine();
						}

						pRec.setStatus('L');
						pRec.store();
					}
				}

				pHead.setStatus('L');
				pHead.store();
			}

			bWriter.close();
		}
	}

	private void createInvoiceFiles(String fileName1, String fileName2, String fileName3, String schoolCategory, IWTimestamp executionDate, Locale currentLocale, String periodText, IFSCheckHeader checkHeader) throws IOException {
		Collection iHeaders = null;
		try {
			iHeaders = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).findByStatusAndCategory("P", schoolCategory);
		}
		catch (IDOLookupException e2) {
			e2.printStackTrace();
		}
		catch (FinderException e2) {
			e2.printStackTrace();
		}
		try {
			createInvoiceSigningFilesExcel(fileName2 + ".xls", "Faktureringsattestlista Barnomsorg, " + executionDate.getDateString("yyyy-MM-dd"), true);
		}
		catch (IOException e3) {
			e3.printStackTrace();
		}
		catch (IDOException e3) {
			e3.printStackTrace();
		}
		try {
			createDeviationFileExcel(iHeaders, fileName3 + ".xls", "Faktureringsavvikelselista Barnomsorg, " + executionDate.getDateString("yyyy-MM-dd"));
		}
		catch (IOException e4) {
			e4.printStackTrace();
		}
		catch (FinderException e4) {
			e4.printStackTrace();
		}
		StringBuffer empty = new StringBuffer("");
		for (int i = 0; i < 25; i++) {
			empty.append("          ");
		}

		if (iHeaders != null && !iHeaders.isEmpty()) {
			NumberFormat format = NumberFormat.getInstance(currentLocale);
			format.setMaximumFractionDigits(0);
			format.setMinimumFractionDigits(0);
			format.setMinimumIntegerDigits(15);
			format.setMaximumIntegerDigits(15);
			format.setGroupingUsed(false);
			NumberFormat format2 = NumberFormat.getInstance(currentLocale);
			format2.setMaximumFractionDigits(0);
			format2.setMinimumFractionDigits(0);
			format2.setMinimumIntegerDigits(10);
			format2.setMaximumIntegerDigits(10);
			format2.setGroupingUsed(false);
			NumberFormat format3 = NumberFormat.getInstance(currentLocale);
			format3.setMaximumFractionDigits(0);
			format3.setMinimumFractionDigits(0);
			format3.setMinimumIntegerDigits(18);
			format3.setMaximumIntegerDigits(18);
			format3.setGroupingUsed(false);
			FileWriter writer = new FileWriter(fileName1);
			BufferedWriter bWriter = new BufferedWriter(writer);

			PostingBusiness pb = getIFSBusiness().getPostingBusiness();

			//Posttyp
			bWriter.write("10");
			//Rutinkod
			bWriter.write("270");
			//Kundkod
			bWriter.write("01820");
			//Rutinkod for avsandande rutin
			bWriter.write("744");
			//Framstallandedatum
			bWriter.write(executionDate.getDateString("yyMMdd"));
			//Klockslag
			bWriter.write(executionDate.getDateString("hhmmss"));
			//Kommentar
			bWriter.write(empty.substring(0, 225));
			bWriter.newLine();

			int numberOfHeaders = 0;
			int numberOf62Lines = 0;
			int numberOf63Lines = 0;
			float sum62Lines = 0;
			float sum63Lines = 0;

			Iterator ihIt = iHeaders.iterator();
			while (ihIt.hasNext()) {
				InvoiceHeader iHead = (InvoiceHeader) ihIt.next();
				Collection rec = null;
				try {
					rec = ((InvoiceRecordHome) IDOLookup.getHome(InvoiceRecord.class)).findByInvoiceHeader(iHead);
				}
				catch (IDOLookupException e3) {
					e3.printStackTrace();
				}
				catch (FinderException e3) {
					e3.printStackTrace();
				}
				try {
					User custodian = iHead.getCustodian();
					if (custodian == null) {
						throw new IFSMissingCustodianException("ifs_missing_custodian", "Missing custodian");
					}
					Address mainAddress = getIFSBusiness().getUserBusiness().getUsersMainAddress(iHead.getCustodian());
					if (mainAddress == null) {
						throw new IFSMissingAddressException("ifs_missing_address", "Missing address");
					}
					PostalCode poCode = mainAddress.getPostalCode();
					if (poCode == null) {
						throw new IFSMissingPostalCodeException("ifs_missing_postalcode", "Missing postalcode");
					}

					if (rec != null && !rec.isEmpty()) {
						Iterator irIt = rec.iterator();
						//Posttyp
						bWriter.write("60");
						//Filler, perioden f.o.m., perioden t.o.m
						bWriter.write(empty.substring(0, 21));
						//Filler, 744, Filler
						bWriter.write(empty.substring(0, 15));
						bWriter.write("744");
						bWriter.write(empty.substring(0, 3));
						//Kundnrtyp
						bWriter.write('P');
						//Kundnr
						String pnr = custodian.getPersonalID();
						if (pnr.length() < 10) {
							StringBuffer p = new StringBuffer(pnr);
							while (p.length() < 10)
								p.insert(0, ' ');

							pnr = p.toString();
						}
						else if (pnr.length() > 10) {
							pnr = pnr.substring(2);
						}
						bWriter.write(pnr);
						//Kundnamn
						String name = custodian.getFirstName() + " " + custodian.getLastName();
						if (name.length() < 25) {
							StringBuffer p = new StringBuffer(name);
							while (p.length() < 25)
								p.append(' ');

							name = p.toString();
						}
						else if (name.length() > 25) {
							name = name.substring(0, 25);
						}
						bWriter.write(name);
						//Kundadress
						String address = mainAddress.getStreetAddress();
						if (address.length() < 27) {
							StringBuffer p = new StringBuffer(address);
							while (p.length() < 27)
								p.append(' ');

							address = p.toString();
						}
						else if (address.length() > 27) {
							address = address.substring(0, 27);
						}
						bWriter.write(address);
						//Kundpostnr
						String po = poCode.getPostalCode();
						if (po.length() < 5) {
							StringBuffer p = new StringBuffer(po);
							while (p.length() < 5)
								p.insert(0, ' ');

							po = p.toString();
						}
						else if (po.length() > 5) {
							po = po.substring(0, 5);
						}
						bWriter.write(po);
						//Kundort
						String poName = poCode.getName();
						if (poName.length() < 13) {
							StringBuffer p = new StringBuffer(poName);
							while (p.length() < 13)
								p.append(' ');

							poName = p.toString();
						}
						else if (poName.length() > 13) {
							poName = poName.substring(0, 13);
						}
						bWriter.write(poName);
						//C/O address
						Address ad = getIFSBusiness().getUserBusiness().getUsersCoAddress(custodian);
						String co = "";
						if (ad != null) {
							co = ad.getStreetAddress();
						}

						if (co.length() < 25) {
							StringBuffer p = new StringBuffer(co);
							while (p.length() < 25)
								p.append(' ');

							co = p.toString();
						}
						else if (co.length() > 25) {
							co = co.substring(0, 25);
						}
						bWriter.write(co);
						//Filler
						bWriter.write(empty.substring(0, 8));
						//Er referens
						StringBuffer bun = new StringBuffer("Kundvalsgruppen Tel: 718 80 00");
						while (bun.length() < 40) {
							bun.append(' ');
						}
						bWriter.write(bun.toString());
						//Avsertyp
						bWriter.write("BARNOMSORG");
						//Filler
						bWriter.write(empty.substring(0, 25));
						//Verksamhetskod
						bWriter.write("BO");
						//Filler
						bWriter.write(empty.substring(0, 15));

						bWriter.newLine();

						//Extra line for BETA system
						//Posttype
						bWriter.write("62");
						//Filler
						bWriter.write(empty.substring(0, 10));
						//Belopp
						bWriter.write("000000000000000");
						//Antal
						bWriter.write("000000000000000");
						//Apris
						bWriter.write("000000000000");
						//moms, filler
						bWriter.write(empty.substring(0, 1));
						bWriter.write("0");
						//Avser period f.o.m
						bWriter.write("00000000");
						//Avser period t.o.m
						bWriter.write("00000000");
						//Faktura text 1 + Filler
						if (periodText.length() < 36) {
							StringBuffer p = new StringBuffer(periodText);
							while (p.length() < 36)
								p.append(' ');

							periodText = p.toString();
						}
						else if (periodText.length() > 36) {
							periodText = periodText.substring(0, 36);
						}
						bWriter.write(periodText);
						//Faktura text 2
						bWriter.write("--------------------");
						//Faktura text 2, 3 and 4
						bWriter.write(empty.substring(0, 88));
						//Kod
						bWriter.write('T');
						//Filler
						bWriter.write(empty.substring(0, 33));
						numberOf62Lines++;
						bWriter.newLine();

						while (irIt.hasNext()) {
							InvoiceRecord iRec = (InvoiceRecord) irIt.next();

							String posting = iRec.getOwnPosting();
							if (posting == null) {
								throw new IFSMissingCheckTaxaException("ifs_missing_checktaxa", "Missing checktaxa");
							}

							if (iRec.getAmount() != 0.0f) {
								//Posttype
								bWriter.write("62");
								//Filler
								bWriter.write(empty.substring(0, 10));
								//Tecken
								boolean isNegative = false;
								float am = iRec.getAmount();
								if (am < 0)
									isNegative = true;

								//Belopp
								am = AccountingUtil.roundAmount(Math.abs(am * 100));
								String amount = format.format(am);
								if (isNegative)
									amount = amount.substring(0, 14) + "�";
								bWriter.write(amount);
								//Antal, pris,
								bWriter.write("000000000000100");
								bWriter.write(amount.substring(3, 15));
								//moms, filler
								bWriter.write(empty.substring(0, 2));
								//Avser period f.o.m
								bWriter.write(empty.substring(0, 8));
								//Avser period t.o.m
								bWriter.write(empty.substring(0, 8));
								//Faktura text 1
								boolean insertLRow = false;
								String LText = null;
								String text = iRec.getInvoiceText();
								if (text == null)
									text = "";
								if (text.length() < 25) {
									StringBuffer t = new StringBuffer(text);
									while (t.length() < 25) {
										t.append(' ');
									}
									text = t.toString();
								}
								else if (text.length() > 25) {
									text = text.substring(0, 25);
								}

								LText = iRec.getInvoiceText2();
								if (LText != null && !"".equals(LText))
									insertLRow = true;

								bWriter.write(text);
								//Filler
								bWriter.write(empty.substring(0, 11));
								//Faktura text 2, 3 and 4
								bWriter.write(empty.substring(0, 108));
								//Kod
								bWriter.write('T');
								//Filler
								bWriter.write(empty.substring(0, 33));

								numberOf62Lines++;
								if (!isNegative)
									sum62Lines += am;
								else
									sum62Lines -= am;
								bWriter.newLine();

								if (insertLRow) {
									//Posttype
									bWriter.write("62");
									//Filler
									bWriter.write(empty.substring(0, 10));
									//Belopp
									bWriter.write(format.format(0.0f));
									//Antal, pris,
									bWriter.write("000000000000000");
									bWriter.write("000000000000");
									//moms, filler
									bWriter.write(empty.substring(0, 2));
									//Avser period f.o.m
									bWriter.write(empty.substring(0, 8));
									//Avser period t.o.m
									bWriter.write(empty.substring(0, 8));
									//Faktura text 1
									if (LText.length() < 36) {
										StringBuffer t = new StringBuffer(LText);
										while (t.length() < 36) {
											t.append(' ');
										}
										LText = t.toString();
									}
									else if (LText.length() > 36) {
										LText = LText.substring(0, 36);
									}

									bWriter.write(LText);
									//Faktura text 2, 3 and 4
									bWriter.write(empty.substring(0, 108));
									//Kod
									bWriter.write('L');
									//Filler
									bWriter.write(empty.substring(0, 33));

									numberOf62Lines++;
									bWriter.newLine();
								}

								bWriter.write("63");
								//Filler
								bWriter.write(empty.substring(0, 6));
								//Belopp
								bWriter.write(amount);
								//Kvantitet and Apris
								bWriter.write("000000000000100");
								bWriter.write(amount.substring(3, 15));
								//Ansvar
								String tmp = pb.findFieldInStringByName(posting, "Ansvar");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Konto
								tmp = pb.findFieldInStringByName(posting, "Konto");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Resurs
								bWriter.write(empty.substring(0, 10));
								//Verksamhet
								tmp = pb.findFieldInStringByName(posting, "Verksamhet");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Aktivitet
								tmp = pb.findFieldInStringByName(posting, "Aktivitet");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Project
								tmp = pb.findFieldInStringByName(posting, "Projekt");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Object
								tmp = pb.findFieldInStringByName(posting, "Objekt");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Motpart
								tmp = pb.findFieldInStringByName(posting, "Motpart");
								if (tmp.length() < 10) {
									StringBuffer post = new StringBuffer(tmp);
									while (post.length() < 10)
										post.append(' ');
									tmp = post.toString();
								}
								else if (tmp.length() > 10) {
									tmp = tmp.substring(0, 10);
								}
								bWriter.write(tmp);
								//Anlaggnings nummber
								bWriter.write(empty.substring(0, 10));
								//Internranta
								bWriter.write(empty.substring(0, 10));
								//Filler
								bWriter.write(empty.substring(0, 100));

								numberOf63Lines++;
								if (!isNegative)
									sum63Lines += am;
								else
									sum63Lines -= am;
								bWriter.newLine();
							}
						}

						numberOfHeaders++;
					}

					iHead.setStatus('L');
					iHead.store();
				}
				catch (IFSMissingCustodianException e) {
					IFSCheckRecordHome home = getIFSCheckRecordHome();
					if (home != null) {
						try {
							IFSCheckRecord ifs_rec = home.create();
							ifs_rec.setHeader(checkHeader);
							ifs_rec.setError(e.getTextKey());
							ifs_rec.setErrorConcerns("Faktura " + ((Integer) iHead.getPrimaryKey()).toString());
							ifs_rec.store();
						}
						catch (CreateException e1) {
							e1.printStackTrace();
						}
					}
				}
				catch (IFSMissingAddressException e) {
					IFSCheckRecordHome home = getIFSCheckRecordHome();
					if (home != null) {
						try {
							IFSCheckRecord ifs_rec = home.create();
							ifs_rec.setHeader(checkHeader);
							ifs_rec.setError(e.getTextKey());
							ifs_rec.setErrorConcerns("Fakturamottagare " + iHead.getCustodian().getPersonalID());
							ifs_rec.store();
						}
						catch (CreateException e1) {
							e1.printStackTrace();
						}
					}
				}
				catch (IFSMissingPostalCodeException e) {
					IFSCheckRecordHome home = getIFSCheckRecordHome();
					if (home != null) {
						try {
							IFSCheckRecord ifs_rec = home.create();
							ifs_rec.setHeader(checkHeader);
							ifs_rec.setError(e.getTextKey());
							ifs_rec.setErrorConcerns("Fakturamottagare " + iHead.getCustodian().getPersonalID());
							ifs_rec.store();
						}
						catch (CreateException e1) {
							e1.printStackTrace();
						}
					}
				}
				catch (IFSMissingCheckTaxaException e) {
					IFSCheckRecordHome home = getIFSCheckRecordHome();
					if (home != null) {
						try {
							IFSCheckRecord ifs_rec = home.create();
							ifs_rec.setHeader(checkHeader);
							ifs_rec.setError(e.getTextKey());
							ifs_rec.setErrorConcerns("Fakturamottagare " + iHead.getCustodian().getPersonalID());
							ifs_rec.store();
						}
						catch (CreateException e1) {
							e1.printStackTrace();
						}
					}
				}
			}

			//Posttyp
			bWriter.write("50");
			//Filler
			bWriter.write(empty.substring(0, 94));
			//Antal poster posttyp 60
			bWriter.write(format2.format(numberOfHeaders));
			//Filler
			bWriter.write(empty.substring(0, 10));
			//Antalet poster posttyp 62
			bWriter.write(format2.format(numberOf62Lines));
			//Antalet poster posttyp 63
			bWriter.write(format2.format(numberOf63Lines));
			//Summen av alla fakturabelopp
			bWriter.write(format3.format(sum62Lines));
			//Summen av alla bokforings belopp
			bWriter.write(format3.format(sum63Lines));
			//Filler
			bWriter.write(empty.substring(0, 78));
			bWriter.newLine();

			bWriter.close();
		}
	}

	/*
	 * private void createInvoiceFilesOld(String fileName1, String fileName2,
	 * String fileName3, String schoolCategory, IWTimestamp executionDate, Locale
	 * currentLocale, String periodText, IFSCheckHeader checkHeader) throws
	 * IOException { Collection iHeaders = null; try { iHeaders =
	 * ((InvoiceHeaderHome)
	 * IDOLookup.getHome(InvoiceHeader.class)).findByStatusAndCategory("P",
	 * schoolCategory); } catch (IDOLookupException e2) { e2.printStackTrace(); }
	 * catch (FinderException e2) { e2.printStackTrace(); } try {
	 * createInvoiceSigningFilesExcel(fileName2 + ".xls",
	 * "Faktureringsattestlista Barnomsorg,
	 * "+executionDate.getDateString("yyyy-MM-dd"), true); } catch (IOException
	 * e3) { e3.printStackTrace(); } catch (IDOException e3) {
	 * e3.printStackTrace(); } try { createDeviationFileExcel(iHeaders,
	 * fileName3+".xls", "Faktureringsavvikelselista Barnomsorg,
	 * "+executionDate.getDateString("yyyy-MM-dd")); } catch (IOException e4) {
	 * e4.printStackTrace(); } catch (FinderException e4) { e4.printStackTrace(); }
	 * StringBuffer empty = new StringBuffer(""); for (int i = 0; i < 25; i++) {
	 * empty.append(" "); }
	 * 
	 * if (iHeaders != null && !iHeaders.isEmpty()) { NumberFormat format =
	 * NumberFormat.getInstance(currentLocale);
	 * format.setMaximumFractionDigits(0); format.setMinimumFractionDigits(0);
	 * format.setMinimumIntegerDigits(14); format.setMaximumIntegerDigits(14);
	 * format.setGroupingUsed(false); NumberFormat format2 =
	 * NumberFormat.getInstance(currentLocale);
	 * format2.setMaximumFractionDigits(0); format2.setMinimumFractionDigits(0);
	 * format2.setMinimumIntegerDigits(10); format2.setMaximumIntegerDigits(10);
	 * format2.setGroupingUsed(false); FileWriter writer = new
	 * FileWriter(fileName1); BufferedWriter bWriter = new
	 * BufferedWriter(writer);
	 * 
	 * PostingBusiness pb = getIFSBusiness().getPostingBusiness();
	 * 
	 * //Posttyp bWriter.write("10"); //Rutinkod + kundkod
	 * bWriter.write(empty.substring(0, 8)); //Rutinkod for avsandande rutin
	 * bWriter.write("KU "); //Framstallandedatum
	 * bWriter.write(executionDate.getDateString("yyMMdd")); //Klockslag
	 * bWriter.write(executionDate.getDateString("hhmmss")); //Kommentar
	 * bWriter.write(empty.substring(0, 225)); bWriter.newLine();
	 * 
	 * int numberOfHeaders = 0;
	 * 
	 * Iterator ihIt = iHeaders.iterator(); while (ihIt.hasNext()) {
	 * InvoiceHeader iHead = (InvoiceHeader) ihIt.next(); Collection rec = null;
	 * try { rec = ((InvoiceRecordHome)
	 * IDOLookup.getHome(InvoiceRecord.class)).findByInvoiceHeader(iHead); }
	 * catch (IDOLookupException e3) { e3.printStackTrace(); } catch
	 * (FinderException e3) { e3.printStackTrace(); } try { User custodian =
	 * iHead.getCustodian(); if (custodian == null) { throw new
	 * IFSMissingCustodianException("ifs_missing_custodian", "Missing
	 * custodian"); } Address mainAddress =
	 * getIFSBusiness().getUserBusiness().getUsersMainAddress(iHead.getCustodian());
	 * if (mainAddress == null) { throw new
	 * IFSMissingAddressException("ifs_missing_address", "Missing address"); }
	 * PostalCode poCode = mainAddress.getPostalCode(); if (poCode == null) {
	 * throw new IFSMissingPostalCodeException("ifs_missing_postalcode", "Missing
	 * postalcode"); }
	 * 
	 * if (rec != null && !rec.isEmpty()) { Iterator irIt = rec.iterator();
	 * //Posttyp bWriter.write("60"); //Filler etc
	 * bWriter.write(empty.substring(0, 21)); //Perioden if (periodText.length()
	 * < 21) { StringBuffer p = new StringBuffer(periodText); while (p.length()
	 * < 21) p.append(' ');
	 * 
	 * periodText = p.toString(); } else if (periodText.length() > 21) {
	 * periodText = periodText.substring(0, 21); } bWriter.write(periodText);
	 * //Kundnrtyp bWriter.write('P'); //Kundnr String pnr =
	 * custodian.getPersonalID(); if (pnr.length() < 10) { StringBuffer p = new
	 * StringBuffer(pnr); while (p.length() < 10) p.insert(0, ' ');
	 * 
	 * pnr = p.toString(); } else if (pnr.length() > 10) { pnr =
	 * pnr.substring(2); } bWriter.write(pnr); //Kundnamn String name =
	 * custodian.getFirstName() + " " + custodian.getLastName(); if
	 * (name.length() < 25) { StringBuffer p = new StringBuffer(name); while
	 * (p.length() < 25) p.append(' ');
	 * 
	 * name = p.toString(); } else if (name.length() > 25) { name =
	 * name.substring(0, 25); } bWriter.write(name); //Kundadress String address =
	 * mainAddress.getStreetAddress(); if (address.length() < 27) { StringBuffer
	 * p = new StringBuffer(address); while (p.length() < 27) p.append(' ');
	 * 
	 * address = p.toString(); } else if (address.length() > 27) { address =
	 * address.substring(0, 27); } bWriter.write(address); //Kundpostnr String po =
	 * poCode.getPostalCode(); if (po.length() < 5) { StringBuffer p = new
	 * StringBuffer(po); while (p.length() < 5) p.insert(0, ' ');
	 * 
	 * po = p.toString(); } else if (po.length() > 5) { po = po.substring(0, 5); }
	 * bWriter.write(po); //Kundort String poName = poCode.getName(); if
	 * (poName.length() < 13) { StringBuffer p = new StringBuffer(poName); while
	 * (p.length() < 13) p.append(' ');
	 * 
	 * poName = p.toString(); } else if (poName.length() > 13) { poName =
	 * poName.substring(0, 13); } bWriter.write(poName); //C/O address Address ad =
	 * getIFSBusiness().getUserBusiness().getUsersCoAddress(custodian); String co =
	 * ""; if (ad != null) { co = ad.getStreetAddress(); }
	 * 
	 * if (co.length() < 25) { StringBuffer p = new StringBuffer(co); while
	 * (p.length() < 25) p.append(' ');
	 * 
	 * co = p.toString(); } else if (co.length() > 25) { co = co.substring(0,
	 * 25); } bWriter.write(co); //Filler bWriter.write(empty.substring(0, 8));
	 * //Er referens StringBuffer bun = new StringBuffer("Kundvalgruppen Tel: 718
	 * 80 00"); while (bun.length() < 40) { bun.append(' '); }
	 * bWriter.write(bun.toString()); //Avsertyp bWriter.write("BARNOMSORG");
	 * //Filler bWriter.write(empty.substring(0, 25)); //Verksamhetskod
	 * bWriter.write("BO"); //Filler bWriter.write(empty.substring(0, 15));
	 * 
	 * bWriter.newLine();
	 * 
	 * while (irIt.hasNext()) { InvoiceRecord iRec = (InvoiceRecord) irIt.next();
	 * 
	 * String posting = iRec.getOwnPosting(); // if //
	 * (iRec.getRegSpecType().getLocalizationKey().equals(RegSpecConstant.CHECK)) // { //
	 * posting = iRec.getDoublePosting(); if (posting == null) { throw new
	 * IFSMissingCheckTaxaException("ifs_missing_checktaxa", "Missing
	 * checktaxa"); } // }
	 * 
	 * if (iRec.getAmount() != 0.0f) { //Posttype bWriter.write("62"); //Filler
	 * bWriter.write(empty.substring(0, 10)); //Tecken float am =
	 * iRec.getAmount(); if (am < 0) bWriter.write('-'); else bWriter.write(' ');
	 * //Belopp 100)); bWriter.write(format.format(am)); //Antal, pris,
	 * bWriter.write("000000000000000"); bWriter.write("000000000000"); //moms,
	 * filler bWriter.write(empty.substring(0, 2)); //Avser period f.o.m
	 * bWriter.write(empty.substring(0, 8)); //Avser period t.o.m
	 * bWriter.write(empty.substring(0, 8)); //Faktura text 1 boolean insertLRow =
	 * false; String LText = null; String text = iRec.getInvoiceText(); if (text ==
	 * null) text = ""; if (text.length() < 36) { StringBuffer t = new
	 * StringBuffer(text); while (t.length() < 36) { t.append(' '); } text =
	 * t.toString(); } else if (text.length() > 36) { text = text.substring(0,
	 * 36); }
	 * 
	 * LText = iRec.getInvoiceText2(); if (LText != null && !"".equals(LText))
	 * insertLRow = true;
	 * 
	 * bWriter.write(text); //Filler //Faktura text 2, 3 and 4
	 * bWriter.write(empty.substring(0, 108)); //Kod bWriter.write('T'); //Filler
	 * bWriter.write(empty.substring(0, 33));
	 * 
	 * bWriter.newLine();
	 * 
	 * if (insertLRow) { //Posttype bWriter.write("62"); //Filler
	 * bWriter.write(empty.substring(0, 10)); //Tecken bWriter.write(' ');
	 * //Belopp bWriter.write(format.format(0.0f)); //Antal, pris,
	 * bWriter.write("000000000000000"); bWriter.write("000000000000"); //moms,
	 * filler bWriter.write(empty.substring(0, 2)); //Avser period f.o.m
	 * bWriter.write(empty.substring(0, 8)); //Avser period t.o.m
	 * bWriter.write(empty.substring(0, 8)); //Faktura text 1 if (LText.length()
	 * < 36) { StringBuffer t = new StringBuffer(LText); while (t.length()
	 * < 36) { t.append(' '); } LText = t.toString(); } else if (LText.length() >
	 * 36) { LText = LText.substring(0, 36); }
	 * 
	 * bWriter.write(LText); //Faktura text 2, 3 and 4
	 * bWriter.write(empty.substring(0, 108)); //Kod bWriter.write('L'); //Filler
	 * bWriter.write(empty.substring(0, 33));
	 * 
	 * bWriter.newLine(); }
	 * 
	 * bWriter.write("63"); //Filler bWriter.write(empty.substring(0, 6));
	 * //Tecken bWriter.write(' '); //Belopp bWriter.write(format.format(am));
	 * //Kvantitet and Apris bWriter.write("000000000000000");
	 * bWriter.write("000000000000"); //Ansvar String tmp =
	 * pb.findFieldInStringByName(posting, "Ansvar"); if (tmp.length() < 10) {
	 * StringBuffer post = new StringBuffer(tmp); while (post.length() < 10)
	 * post.append(' '); tmp = post.toString(); } else if (tmp.length() > 10) {
	 * tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Konto tmp =
	 * pb.findFieldInStringByName(posting, "Konto"); if (tmp.length() < 10) {
	 * StringBuffer post = new StringBuffer(tmp); while (post.length() < 10)
	 * post.append(' '); tmp = post.toString(); } else if (tmp.length() > 10) {
	 * tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Resurs
	 * bWriter.write(empty.substring(0, 10)); //Verksamhet tmp =
	 * pb.findFieldInStringByName(posting, "Verksamhet"); if (tmp.length()
	 * < 10) { StringBuffer post = new StringBuffer(tmp); while (post.length()
	 * < 10) post.append(' '); tmp = post.toString(); } else if (tmp.length() >
	 * 10) { tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Aktivitet tmp =
	 * pb.findFieldInStringByName(posting, "Aktivitet"); if (tmp.length() < 10) {
	 * StringBuffer post = new StringBuffer(tmp); while (post.length() < 10)
	 * post.append(' '); tmp = post.toString(); } else if (tmp.length() > 10) {
	 * tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Project tmp =
	 * pb.findFieldInStringByName(posting, "Projekt"); if (tmp.length() < 10) {
	 * StringBuffer post = new StringBuffer(tmp); while (post.length() < 10)
	 * post.append(' '); tmp = post.toString(); } else if (tmp.length() > 10) {
	 * tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Object tmp =
	 * pb.findFieldInStringByName(posting, "Objekt"); if (tmp.length() < 10) {
	 * StringBuffer post = new StringBuffer(tmp); while (post.length() < 10)
	 * post.append(' '); tmp = post.toString(); } else if (tmp.length() > 10) {
	 * tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Motpart tmp =
	 * pb.findFieldInStringByName(posting, "Motpart"); if (tmp.length() < 10) {
	 * StringBuffer post = new StringBuffer(tmp); while (post.length() < 10)
	 * post.append(' '); tmp = post.toString(); } else if (tmp.length() > 10) {
	 * tmp = tmp.substring(0, 10); } bWriter.write(tmp); //Anlaggnings nummber
	 * bWriter.write(empty.substring(0, 10)); //Internranta
	 * bWriter.write(empty.substring(0, 10)); //Filler
	 * bWriter.write(empty.substring(0, 100));
	 * 
	 * bWriter.newLine(); } }
	 * 
	 * numberOfHeaders++; }
	 * 
	 * iHead.setStatus('L'); iHead.store(); } catch (IFSMissingCustodianException e) {
	 * IFSCheckRecordHome home = getIFSCheckRecordHome(); if (home != null) { try {
	 * IFSCheckRecord ifs_rec = home.create(); ifs_rec.setHeader(checkHeader);
	 * ifs_rec.setError(e.getTextKey()); ifs_rec.setErrorConcerns("Faktura " +
	 * ((Integer) iHead.getPrimaryKey()).toString()); ifs_rec.store(); } catch
	 * (CreateException e1) { e1.printStackTrace(); } } } catch
	 * (IFSMissingAddressException e) { IFSCheckRecordHome home =
	 * getIFSCheckRecordHome(); if (home != null) { try { IFSCheckRecord ifs_rec =
	 * home.create(); ifs_rec.setHeader(checkHeader);
	 * ifs_rec.setError(e.getTextKey());
	 * ifs_rec.setErrorConcerns("Fakturamottagare " +
	 * iHead.getCustodian().getPersonalID()); ifs_rec.store(); } catch
	 * (CreateException e1) { e1.printStackTrace(); } } } catch
	 * (IFSMissingPostalCodeException e) { IFSCheckRecordHome home =
	 * getIFSCheckRecordHome(); if (home != null) { try { IFSCheckRecord ifs_rec =
	 * home.create(); ifs_rec.setHeader(checkHeader);
	 * ifs_rec.setError(e.getTextKey());
	 * ifs_rec.setErrorConcerns("Fakturamottagare " +
	 * iHead.getCustodian().getPersonalID()); ifs_rec.store(); } catch
	 * (CreateException e1) { e1.printStackTrace(); } } } catch
	 * (IFSMissingCheckTaxaException e) { IFSCheckRecordHome home =
	 * getIFSCheckRecordHome(); if (home != null) { try { IFSCheckRecord ifs_rec =
	 * home.create(); ifs_rec.setHeader(checkHeader);
	 * ifs_rec.setError(e.getTextKey());
	 * ifs_rec.setErrorConcerns("Fakturamottagare " +
	 * iHead.getCustodian().getPersonalID()); ifs_rec.store(); } catch
	 * (CreateException e1) { e1.printStackTrace(); } } } }
	 * 
	 * //Posttyp bWriter.write("50"); //Filler bWriter.write(empty.substring(0,
	 * 94)); //Antal poster posttyp 60
	 * bWriter.write(format2.format(numberOfHeaders)); //Filler, etc...
	 * bWriter.write(empty.substring(0, 144)); bWriter.newLine();
	 * 
	 * bWriter.close(); }
	 */

	private void createDeviationFileExcel(Collection data, String fileName, String headerText) throws IOException, FinderException {
		if (data != null && !data.isEmpty()) {
			int[] columnWidths = { 15, 25, 15, 25 };
			String[] columnNames = { "Fakturaperiod", "Fakturmottagars personnummer", "Belopp", "Avvikelse orsak" };
			createExcelWorkBook(columnWidths, columnNames, headerText);
			HSSFSheet sheet = wb.getSheet("Excel");
			short rowNumber = (short) (sheet.getLastRowNum() + 1);
			short cellNumber = 0;
			float totalAmount = 0;
			float recordAmount;
			Iterator it = data.iterator();
			createStyleAlignRight();
			createStyleBold();
			createStyleBoldAlignRight();
			while (it.hasNext()) {

				InvoiceHeader iHead = (InvoiceHeader) it.next();
				ArrayList iRecs = new ArrayList(((InvoiceRecordHome) IDOLookup.getHome(InvoiceRecord.class)).findByInvoiceHeader(iHead));
				if (!iRecs.isEmpty()) {
					Iterator irIt = iRecs.iterator();

					while (irIt.hasNext()) {
						InvoiceRecord iRec = (InvoiceRecord) irIt.next();
						recordAmount = AccountingUtil.roundAmount(iRec.getAmount());
						if (recordAmount >= 0) {
							if (hasInvoiceHeaderDeviations(iHead) || hasInvoiceRecordDeviations(iRec)) {
								totalAmount += recordAmount;
								row = sheet.createRow(rowNumber++);
								row.createCell(cellNumber++).setCellValue(iHead.getPeriod().toString());
								if (iHead.getCustodian() != null)
									row.createCell(cellNumber++).setCellValue(iHead.getCustodian().getPersonalID());
								else
									cellNumber++;
								cell = row.createCell(cellNumber++);
								cell.setCellValue(getNumberFormat().format(recordAmount));
								cell.setCellStyle(getStyleAlignRight());
								row.createCell(cellNumber++).setCellValue(getDeviationString());
								cellNumber = 0;
								setDeviationString("");
							}
						}
					}
				}
			}
			row = sheet.createRow(rowNumber++);
			cell = row.createCell(cellNumber);
			cell.setCellValue("Summa");
			cell.setCellStyle(getStyleBold());
			cell = row.createCell(cellNumber += 2);
			cell.setCellValue(getNumberFormat().format(totalAmount));
			cell.setCellStyle(getStyleBoldAlignRight());
			saveExcelWorkBook(fileName, wb);
		}
	}

	private boolean hasInvoiceHeaderDeviations(InvoiceHeader iHead) {
		if (iHead.getCustodian() == null) {
			setDeviationString("Saknas fakturamottagare");
			return true;
		}
		else if (iHead.getCustodian().getAddresses().size() == 0) {
			setDeviationString("Saknas faktura adress");
			return true;
		}
		return false;
	}

	private boolean hasInvoiceRecordDeviations(InvoiceRecord iRec) {
		if (hasNoCheck(iRec)) {
			setDeviationString("Saknas check");
			return true;
		}

		try {
			PostingBusiness pb = getIFSBusiness().getPostingBusiness();
			IWTimestamp now = IWTimestamp.RightNow();
			pb.validateString(iRec.getOwnPosting(), now.getDate());
		}
		catch (PostingException e) {
			e.printStackTrace();
			setDeviationString("Posting failed");
			return true;
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean hasNoCheck(InvoiceRecord iRec) {
		return iRec.getChildCareContract() == null || iRec.getChildCareContract().getApplication() == null || iRec.getChildCareContract().getApplication().getCheck() == null;
	}

	private void createPaymentFilesExcel(Collection data, String fileName, String headerText, boolean doublePosting) throws IOException {
		if (data != null && !data.isEmpty()) {
			int[] columnWidths = { 11, 7, 6, 7, 10, 8, 7, 7, 7, 10, 35 };
			String[] columnNames = { "Bokf datum", "Ansvar", "Konto", "Resurs", "Verksamhet", "Aktivitet", "Projekt", "Objekt", "Motpart", "Belopp", "Text" };
			createExcelWorkBook(columnWidths, columnNames, headerText);
			HSSFSheet sheet = wb.getSheet("Excel");
			short rowNumber = (short) (sheet.getLastRowNum() + 1);
			short cellNumber;
			//			HSSFHeader header = sheet.getHeader();
			//		    header.setLeft(headerText);
			//			header.setRight("Sida "+HSSFHeader.page());
			//			sheet.getPrintSetup().setLandscape(true);
			float totalAmount = 0;
			float amount;
			PostingBusiness pb = getIFSBusiness().getPostingBusiness();
			Iterator it = data.iterator();
			int numberOfRecords = 0;
			createStyleAlignRight();
			while (it.hasNext()) {
				cellNumber = 0;
				PaymentRecord pRec = (PaymentRecord) it.next();
				if (pRec.getTotalAmount() != 0.0f) {
					amount = AccountingUtil.roundAmount(pRec.getTotalAmount());
					totalAmount += amount;
					numberOfRecords++;
					row = sheet.createRow(rowNumber++);
					row.createCell(cellNumber++).setCellValue(_paymentDate.getDateString("yyyy-MM-dd"));
					short loopTillEndOfPostingFields = (short) (cellNumber + 8);
					for (short i = cellNumber; i < loopTillEndOfPostingFields; i++)
						row.createCell(cellNumber++).setCellValue(pb.findFieldInStringByName(pRec.getOwnPosting(), columnNames[i]));
					cell = row.createCell(cellNumber++);
					cell.setCellValue(getNumberFormat().format(amount));
					cell.setCellStyle(getStyleAlignRight());
					row.createCell(cellNumber++).setCellValue(pRec.getPaymentText());
					if (doublePosting) {
						cellNumber = 0;
						numberOfRecords++;
						row = sheet.createRow(rowNumber++);
						row.createCell(cellNumber++).setCellValue(_paymentDate.getDateString("yyyy-MM-dd"));
						for (short i = cellNumber; i < loopTillEndOfPostingFields; i++)
							row.createCell(cellNumber++).setCellValue(pb.findFieldInStringByName(pRec.getDoublePosting(), columnNames[i]));
						cell = row.createCell(cellNumber++);
						cell.setCellValue(getNumberFormat().format(-1 * amount));
						cell.setCellStyle(getStyleAlignRight());
						row.createCell(cellNumber++).setCellValue(pRec.getPaymentText());
					}
				}
			}
			//sheet.createRow(rowNumber++).createCell((short) (row.getLastCellNum()
			// - 1)).setCellValue(totalAmount);
			sheet.createRow(rowNumber += 2).createCell(row.getFirstCellNum()).setCellValue(numberOfRecords + " bokf�ringsposter,   Kreditbelopp totalt:  - " + getNumberFormat().format(totalAmount) + ",   Debetbelopp totalt: " + getNumberFormat().format(totalAmount));
			saveExcelWorkBook(fileName, wb);
		}
	}

	private void createInvoiceSigningFilesExcel(String fileName, String headerText, boolean signingFooter) throws IOException, IDOException {
		wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Excel");
		sheet.setColumnWidth((short) 0, (short) (30 * 256));
		sheet.setColumnWidth((short) 1, (short) (20 * 256));
		sheet.setColumnWidth((short) 2, (short) (20 * 256));
		short rowNumber = 0;
		short cellNumber = 0;
		row = sheet.createRow(rowNumber++);
		if (!headerText.equals("")) {
			row.createCell(cellNumber++).setCellValue(headerText);
			rowNumber++;
			row = sheet.createRow(rowNumber += 4);
		}
		CalendarMonth currentMonth = new CalendarMonth();
		CalendarMonth previousMonth = currentMonth.getPreviousCalendarMonth();
		int numberOfInvoicesForCurrentMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfInvoicesForCurrentMonth();
		int numberOfInvoicesForPreviousMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfInvoicesForMonth(previousMonth);
		int numberOfChildrenForCurrentMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfChildrenForCurrentMonth();
		int numberOfChildrenForPreviousMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfChildrenForMonth(previousMonth);
		int totalInvoiceRecordAmountForCurrentMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getTotalInvoiceRecordAmountForCurrentMonth();
		int totalInvoiceRecordAmountFoPreviousMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getTotalInvoiceRecordAmountForMonth(previousMonth);
		row = sheet.createRow(rowNumber++);
		row.createCell(cellNumber++).setCellValue("Innevarande m�nad");
		row.createCell(cellNumber).setCellValue("F�reg�ende m�nad");
		row = sheet.createRow(rowNumber++);
		row.createCell(cellNumber--).setCellValue(numberOfInvoicesForPreviousMonth);
		row.createCell(cellNumber--).setCellValue(numberOfInvoicesForCurrentMonth);
		row.createCell(cellNumber).setCellValue("Total antal generade fakturor");
		row = sheet.createRow(rowNumber++);
		row.createCell(cellNumber++).setCellValue("Total antal behandlade indvider");
		row.createCell(cellNumber++).setCellValue(numberOfChildrenForCurrentMonth);
		row.createCell(cellNumber).setCellValue(numberOfChildrenForPreviousMonth);
		row = sheet.createRow(rowNumber++);
		row.createCell(cellNumber--).setCellValue(totalInvoiceRecordAmountFoPreviousMonth);
		row.createCell(cellNumber--).setCellValue(totalInvoiceRecordAmountForCurrentMonth);
		row.createCell(cellNumber).setCellValue("Totalt fakturerat belopp");
		if (signingFooter) {
			createSigningFooter(sheet, rowNumber);
		}
		saveExcelWorkBook(fileName, wb);
	}

	private void createPaymentSigningFilesExcel(Collection data, String fileName, String headerText, boolean signingFooter) throws IOException, FinderException {
		if (data != null && !data.isEmpty()) {
			int[] columnWidths = { 25, 35, 12, 12 };
			String[] columnNames = { "Anordnare", "Text", "Placeringer", "Belopp" };
			createExcelWorkBook(columnWidths, columnNames, headerText);
			HSSFSheet sheet = wb.getSheet("Excel");
			short rowNumber = (short) (sheet.getLastRowNum() + 1);
			short cellNumber = 0;
			ArrayList paymentHeaders = new ArrayList(data);
			Collections.sort(paymentHeaders, new PaymentComparator());
			Iterator it = paymentHeaders.iterator();
			boolean firstRecord;
			float recordAmount;
			float totalHeaderAmount = 0;
			float totalAmount = 0;
			int totalHeaderStudents = 0;
			int totalStudents = 0;
			School school = null;
			createStyleAlignRight();
			createStyleBold();
			createStyleBoldAlignRight();
			createStyleItalicUnderlineAlignRight();
			while (it.hasNext()) {
				PaymentHeader pHead = (PaymentHeader) it.next();
				Collection pRecs = ((PaymentRecordHome) IDOLookup.getHome(PaymentRecord.class)).findByPaymentHeader(pHead);
				if (!pRecs.isEmpty()) {
					Iterator prIt = pRecs.iterator();
					firstRecord = true;
					school = pHead.getSchool();
					row = sheet.createRow(rowNumber++);
					row.createCell(cellNumber++).setCellValue(school.getName());

					while (prIt.hasNext()) {
						PaymentRecord pRec = (PaymentRecord) prIt.next();
						if (!firstRecord)
							row = sheet.createRow(rowNumber++);
						row.createCell(cellNumber++).setCellValue(pRec.getPaymentText());
						recordAmount = AccountingUtil.roundAmount(pRec.getTotalAmount());
						totalHeaderAmount += recordAmount;
						totalHeaderStudents += pRec.getPlacements();
						cell = row.createCell(cellNumber++);
						cell.setCellValue(pRec.getPlacements());
						cell.setCellStyle((getStyleAlignRight()));
						cell = row.createCell(cellNumber--);
						cell.setCellValue(getNumberFormat().format(recordAmount));
						cell.setCellStyle((getStyleAlignRight()));
						cellNumber--;
						if (!prIt.hasNext()) {
							cellNumber--;
							row = sheet.createRow(rowNumber++);							
							row.createCell(cellNumber++).setCellValue("");
							row.createCell(cellNumber++).setCellValue("Summa");
							row.createCell(cellNumber++).setCellValue(totalHeaderStudents);
							row.createCell(cellNumber--).setCellValue(getNumberFormat().format(totalHeaderAmount));					
						}
						firstRecord = false;
					}
					cellNumber -= 2;
					totalAmount += totalHeaderAmount;
					totalHeaderAmount = 0;
					totalStudents += totalHeaderStudents;
					totalHeaderStudents = 0;
					for (short i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
						row.getCell(i).setCellStyle(getStyleItalicUnderlineAlignRight());
					}
				}
			}
			row = sheet.createRow(rowNumber++);
			cell = row.createCell(cellNumber += 2);
			cell.setCellValue(getNumberFormat().format(totalStudents));
			cell.setCellStyle(getStyleBoldAlignRight());
			cell = row.createCell(cellNumber += 1);
			cell.setCellValue(getNumberFormat().format(totalAmount));
			cell.setCellStyle(getStyleBoldAlignRight());
			rowNumber++;
			row = sheet.createRow(rowNumber++);
			cell = row.createCell(cellNumber -= 3);
			if (!signingFooter) {
				setInCommuneSum(totalAmount);
			}
			else {
				cell.setCellValue("Summa fr�n egna kommunala anordnare");
				cell = row.createCell(cellNumber+=3);
				cell.setCellValue(getNumberFormat().format(getInCommuneSum()));
				cell.setCellStyle(getStyleAlignRight());
				row = sheet.createRow(rowNumber++);
				cell = row.createCell(cellNumber-=3);
				cell.setCellValue("Summa fr�n �vriga anordnare");
				cell = row.createCell(cellNumber+=3);
				cell.setCellValue(getNumberFormat().format(totalAmount - getInCommuneSum()));
				cell.setCellStyle(getStyleAlignRight());
				row = sheet.createRow(rowNumber++);
				cell = row.createCell(cellNumber-=3);
			}
			cell.setCellValue("Bruttosumma att utbetala");
			cell.setCellStyle(getStyleBold());
			cell = row.createCell(cellNumber+=3);
			cell.setCellValue(getNumberFormat().format(totalAmount));
			cell.setCellStyle(getStyleBoldAlignRight());

			if (signingFooter) {
				createSigningFooter(sheet, rowNumber);
			}
			saveExcelWorkBook(fileName, wb);
		}
	}

	private void createSigningFooter(HSSFSheet sheet, short rowNumber) {
		short cellNumber = 1;
		createStyleBold();
		row = sheet.createRow(rowNumber += 4);
		cell = row.createCell(cellNumber--);
		cell.setCellValue("Attestering");
		cell.setCellStyle(getStyleBold());
		rowNumber += 4;
		createSigningFooterDetail(sheet, rowNumber, cellNumber, "Granskingsattest");
		rowNumber = createSigningFooterDetail(sheet, rowNumber, cellNumber += 2, "Beslutsattest");
		rowNumber = createSigningFooterDetail(sheet, rowNumber += 5, cellNumber -= 2, "Beh�righetsattest");
	}

	private short createSigningFooterDetail(HSSFSheet sheet, short rowNumber, short cellNumber, String text) {
		row = sheet.createRow(rowNumber);
		cell = row.createCell(cellNumber);
		cell.setCellValue(text);
		row = sheet.createRow(rowNumber += 2);
		cell = row.createCell(cellNumber);
		cell.setCellValue("Datum...............................");
		row = sheet.createRow(rowNumber += 2);
		cell = row.createCell(cellNumber);
		cell.setCellValue("...........................................");
		return rowNumber;
	}

	private void createExcelWorkBook(int[] columnWidths, String[] columnNames, String headerText) {
		wb = new HSSFWorkbook();
		createStyleBoldUnderlineAlignRight();
		createStyleBoldUnderline();
		HSSFSheet sheet = wb.createSheet("Excel");
		for (short i = 0; i < columnWidths.length; i++)
			sheet.setColumnWidth(i, (short) (columnWidths[i] * 256));

		short rowNumber = 0;
		row = sheet.createRow(rowNumber++);
		if (!headerText.equals("")) {
			row.createCell((short) 0).setCellValue(headerText);
			rowNumber++;
			row = sheet.createRow(rowNumber++);
		}
		for (short i = 0; i < columnNames.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(columnNames[i]);
			if (columnNames[i].equals("Belopp"))
				cell.setCellStyle(getStyleBoldUnderlineAlignRight());
			else
				cell.setCellStyle(getStyleBoldUnderline());
		}
	}

	private void saveExcelWorkBook(String fileName, HSSFWorkbook wb) throws IOException {
		FileOutputStream out = new FileOutputStream(fileName);
		wb.write(out);
		out.close();
	}

	private IFSBusiness getIFSBusiness() {
		try {
			return (IFSBusiness) IBOLookup.getServiceInstance(_iwac, IFSBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	private IFSCheckRecordHome getIFSCheckRecordHome() {
		try {
			return (IFSCheckRecordHome) IDOLookup.getHome(IFSCheckRecord.class);
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}

		return null;
	}
	private void createNumberFormat() {
		numberFormat = NumberFormat.getInstance(Locale.FRENCH);
		numberFormat.setMaximumFractionDigits(0);
		numberFormat.setMinimumIntegerDigits(1);
	}

	private NumberFormat getNumberFormat() {
		return numberFormat;
	}

	private String getDeviationString() {
		return deviationString;
	}

	private void setDeviationString(String _deviationString) {
		deviationString = _deviationString;
	}

	public HSSFCellStyle getStyleAlignRight() {
		return styleAlignRight;
	}

	public HSSFCellStyle getStyleBold() {
		return styleBold;
	}

	public HSSFCellStyle getStyleBoldAlignRight() {
		return styleBoldAlignRight;
	}

	public HSSFCellStyle getStyleBoldUnderline() {
		return styleBoldUnderline;
	}

	public HSSFCellStyle getStyleBoldUnderlineAlignRight() {
		return styleBoldUnderlineAlignRight;
	}

	public HSSFCellStyle getStyleItalicUnderlineAlignRight() {
		return styleItalicUnderlineAlignRight;
	}
	
	private HSSFCellStyle createStyleAlignRight() {
		styleAlignRight = wb.createCellStyle();
		styleAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		return styleAlignRight;
	}
	
	private HSSFCellStyle createStyleBold() {
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleBold = wb.createCellStyle();
		styleBold.setFont(font);
		return styleBold;
	}

	private HSSFCellStyle createStyleBoldAlignRight() {
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleBoldAlignRight = wb.createCellStyle();
		styleBoldAlignRight.setFont(font);
		styleBoldAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		return styleBoldAlignRight;
	}

	private HSSFCellStyle createStyleBoldUnderline() {
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleBoldUnderline = wb.createCellStyle();
		styleBoldUnderline.setFont(font);
		styleBoldUnderline.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		return styleBoldUnderline;
	}
	
	private HSSFCellStyle createStyleBoldUnderlineAlignRight() {
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleBoldUnderlineAlignRight = wb.createCellStyle();
		styleBoldUnderlineAlignRight.setFont(font);
		styleBoldUnderlineAlignRight.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleBoldUnderlineAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		return styleBoldUnderlineAlignRight;
	}

	private HSSFCellStyle createStyleItalicUnderlineAlignRight() {
		HSSFFont italicFont = wb.createFont();
		italicFont.setItalic(true);
		styleItalicUnderlineAlignRight = wb.createCellStyle();
		styleItalicUnderlineAlignRight.setFont(italicFont);
		styleItalicUnderlineAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleItalicUnderlineAlignRight.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		return styleItalicUnderlineAlignRight;
	}

	public float getInCommuneSum() {
		return inCommuneSum;
	}

	public void setInCommuneSum(float inCommuneSum) {
		this.inCommuneSum = inCommuneSum;
	}

}