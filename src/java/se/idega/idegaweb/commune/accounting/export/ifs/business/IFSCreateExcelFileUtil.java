/*
 * $Id: IFSCreateExcelFileUtil.java,v 1.2 2006/04/09 11:53:33 laddi Exp $ Created on Jan
 * 21, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package se.idega.idegaweb.commune.accounting.export.ifs.business;

import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import javax.ejb.FinderException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import se.idega.idegaweb.commune.accounting.business.AccountingUtil;
import se.idega.idegaweb.commune.accounting.business.PaymentComparator;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceHeader;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceHeaderHome;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecord;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecordHome;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentHeader;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecord;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecordHome;
import se.idega.idegaweb.commune.accounting.posting.business.PostingBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingException;
import se.idega.idegaweb.commune.care.business.CareBusiness;
import se.idega.idegaweb.commune.care.data.ChildCareContract;
import com.idega.block.school.data.School;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDOException;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.util.CalendarMonth;
import com.idega.util.IWTimestamp;

/**
 * 
 * Last modified: $Date: 2006/04/09 11:53:33 $ by $Author: laddi $
 * 
 * @author <a href="mailto:palli@idega.com">palli </a>
 * @version $Revision: 1.2 $
 */
public class IFSCreateExcelFileUtil {
	
	protected final static int FILE_TYPE_OWN_POSTING = 1;

	protected final static int FILE_TYPE_DOUBLE_POSTING = 2;

	protected final static int FILE_TYPE_KOMMUN = 3;	

	private HSSFWorkbook wb = null;

	private HSSFRow row = null;

	private HSSFCell cell = null;

	private HSSFCellStyle styleAlignRight = null;

	private HSSFCellStyle styleBold = null;

	private HSSFCellStyle styleBoldAlignRight = null;

	private HSSFCellStyle styleBoldUnderline = null;

	private HSSFCellStyle styleBoldUnderlineAlignRight = null;

	private HSSFCellStyle styleItalicUnderlineAlignRight = null;

	private long inCommuneSum = 0;

	private NumberFormat numberFormat = null;

	private String deviationString = "";
	
	protected IWApplicationContext iwac = null;
	
	protected IWTimestamp paymentDate = null;

	public IFSCreateExcelFileUtil(IWApplicationContext iwac, IWTimestamp paymentDate) {
		this.iwac = iwac;
		this.paymentDate = paymentDate;
		createNumberFormat();
	}

	protected void createDeviationFileExcel(Collection data, String fileName, String headerText) throws IOException,
			FinderException {
		if (data != null && !data.isEmpty()) {
			int[] columnWidths = { 15, 20, 12, 35 };
			String[] columnNames = { "Fakturaperiod", "Fakturmottagars pnr", "Belopp", "Avvikelse orsak" };
			createExcelWorkBook(columnWidths, columnNames, headerText);
			HSSFSheet sheet = this.wb.getSheet("Excel");
			short rowNumber = (short) (sheet.getLastRowNum() + 1);
			short cellNumber = 0;
			long totalAmount = 0;
			long recordAmount;
			boolean invoiceHeaderDeviations;
			Iterator it = data.iterator();
			createStyleAlignRight();
			createStyleBold();
			createStyleBoldAlignRight();
			while (it.hasNext()) {
				InvoiceHeader iHead = (InvoiceHeader) it.next();
				ArrayList iRecs = new ArrayList(
						((InvoiceRecordHome) IDOLookup.getHome(InvoiceRecord.class)).findByInvoiceHeader(iHead));
				if (!iRecs.isEmpty()) {
					long headerSum = 0;
					invoiceHeaderDeviations = false;
					for (int i = 0; i < iRecs.size(); i++) {
						headerSum += AccountingUtil.roundAmount(((InvoiceRecord) iRecs.get(i)).getAmount());
					}
					if (headerSum < 0) {
						setDeviationString("Total belopp från faktura huvud är negativt");
						invoiceHeaderDeviations = true;
					}
					else if (iHead.getCustodian() == null) {
						setDeviationString("Saknas fakturamottagare");
						invoiceHeaderDeviations = true;
					}
					else if (iHead.getCustodian().getAddresses().size() == 0) {
						setDeviationString("Saknas faktura adress");
						invoiceHeaderDeviations = true;
					}
					Iterator irIt = iRecs.iterator();
					while (irIt.hasNext()) {
						InvoiceRecord iRec = (InvoiceRecord) irIt.next();
						recordAmount = AccountingUtil.roundAmount(iRec.getAmount());
						if (recordAmount >= 0 || headerSum < 0) {
							if (invoiceHeaderDeviations || hasInvoiceRecordDeviations(iRec)) {
								totalAmount += recordAmount;
								this.row = sheet.createRow(rowNumber++);
								this.row.createCell(cellNumber++).setCellValue(iHead.getPeriod().toString());
								if (iHead.getCustodian() != null) {
									this.row.createCell(cellNumber++).setCellValue(iHead.getCustodian().getPersonalID());
								}
								else {
									cellNumber++;
								}
								this.cell = this.row.createCell(cellNumber++);
								this.cell.setCellValue(getNumberFormat().format(recordAmount));
								this.cell.setCellStyle(getStyleAlignRight());
								this.row.createCell(cellNumber++).setCellValue(getDeviationString());
								cellNumber = 0;
								if (!invoiceHeaderDeviations) {
									setDeviationString("");
								}
							}
						}
					}
				}
			}
			setDeviationString("");
			this.row = sheet.createRow(rowNumber++);
			this.cell = this.row.createCell(cellNumber);
			this.cell.setCellValue("Summa");
			this.cell.setCellStyle(getStyleBold());
			this.cell = this.row.createCell(cellNumber += 2);
			this.cell.setCellValue(getNumberFormat().format(totalAmount));
			this.cell.setCellStyle(getStyleBoldAlignRight());
			saveExcelWorkBook(fileName, this.wb);
		}
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
		ChildCareContract contract = iRec.getChildCareContract();
		if (contract == null) {
			return true;
		}
		if (contract.getApplication() == null) {
			return true;
		}
		try {
			CareBusiness business = (CareBusiness) IBOLookup.getServiceInstance(this.iwac, CareBusiness.class);
			return !business.hasGrantedCheck(contract.getChild());
		}
		catch (RemoteException re) {
			return true;
		}
	}

	protected void createPaymentFilesExcel(Collection data, String fileName, String headerText, int fileType)
			throws IOException {
		if (data != null && !data.isEmpty()) {
			int[] columnWidths = { 11, 7, 6, 7, 10, 8, 7, 7, 7, 10, 35, 25 };
			String[] columnNames = { "Bokf datum", "Ansvar", "Konto", "Resurs", "Verksamhet", "Aktivitet", "Projekt",
					"Objekt", "Motpart", "Belopp", "Text", "Anordnare" };
			int[] kommunColumnWidths = { 11, 7, 6, 7, 10, 8, 7, 7, 7, 10, 10, 35, 25 };
			String[] kommunColumnNames = { "Bokf datum", "Ansvar", "Konto", "Resurs", "Verksamhet", "Aktivitet",
					"Projekt", "Objekt", "Motpart", "Placeringar", "Belopp", "Text", "Anordnare" };
			if (fileType == FILE_TYPE_KOMMUN) {
				columnWidths = kommunColumnWidths;
				columnNames = kommunColumnNames;
			}
			createExcelWorkBook(columnWidths, columnNames, headerText);
			HSSFSheet sheet = this.wb.getSheet("Excel");
			short rowNumber = (short) (sheet.getLastRowNum() + 1);
			// HSSFHeader header = sheet.getHeader();
			// header.setLeft(headerText);
			// header.setRight("Sida "+HSSFHeader.page());
			// sheet.getPrintSetup().setLandscape(true);
			long totalAmount = 0;
			long amount;
			PostingBusiness pb = getIFSBusiness().getPostingBusiness();
			Iterator it = data.iterator();
			int numberOfRecords = 0;
			createStyleAlignRight();
			while (it.hasNext()) {
				PaymentRecord pRec = (PaymentRecord) it.next();
				School school = pRec.getPaymentHeader().getSchool();
				if (pRec.getTotalAmount() != 0.0f) {
					amount = AccountingUtil.roundAmount(pRec.getTotalAmount());
					if (fileType == FILE_TYPE_OWN_POSTING || fileType == FILE_TYPE_DOUBLE_POSTING) {
						numberOfRecords++;
						rowNumber = createPaymentLine(columnNames, sheet, rowNumber, amount, pb, pRec, school,
								pRec.getOwnPosting(), fileType);
					}
					if (fileType == FILE_TYPE_DOUBLE_POSTING || fileType == FILE_TYPE_KOMMUN) {
						numberOfRecords++;
						rowNumber = createPaymentLine(columnNames, sheet, rowNumber, -1 * amount, pb, pRec, school,
								pRec.getDoublePosting(), fileType);
					}
					totalAmount += amount;
				}
			}
			if (fileType == FILE_TYPE_KOMMUN) {
				setInCommuneSum(totalAmount);
			}
			sheet.createRow(rowNumber += 2).createCell(this.row.getFirstCellNum()).setCellValue(
					numberOfRecords + " bokföringsposter,   Kreditbelopp totalt:  - "
							+ getNumberFormat().format(totalAmount) + ",   Debetbelopp totalt: "
							+ getNumberFormat().format(totalAmount));
			saveExcelWorkBook(fileName, this.wb);
		}
	}

	private short createPaymentLine(String[] columnNames, HSSFSheet sheet, short rowNumber, long amount,
			PostingBusiness pb, PaymentRecord pRec, School school, String postingString, int fileType)
			throws RemoteException {
		short cellNumber = 0;
		this.row = sheet.createRow(rowNumber++);
		this.row.createCell(cellNumber++).setCellValue(this.paymentDate.getDateString("yyyy-MM-dd"));
		short loopTillEndOfPostingFields = (short) (cellNumber + 8);
		for (short i = cellNumber; i < loopTillEndOfPostingFields; i++) {
			this.row.createCell(cellNumber++).setCellValue(pb.findFieldInStringByName(postingString, columnNames[i]));
		}
		if (fileType == FILE_TYPE_KOMMUN) {
			this.cell = this.row.createCell(cellNumber++);
			this.cell.setCellValue(pRec.getPlacements());
		}
		this.cell = this.row.createCell(cellNumber++);
		this.cell.setCellValue(getNumberFormat().format(amount));
		this.cell.setCellStyle(getStyleAlignRight());
		this.row.createCell(cellNumber++).setCellValue(pRec.getPaymentText());
		this.row.createCell(cellNumber++).setCellValue(school.getName());
		return rowNumber;
	}

	protected void createInvoiceSigningFilesExcel(String fileName, String headerText, boolean signingFooter)
			throws IOException, IDOException {
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("Excel");
		sheet.setColumnWidth((short) 0, (short) (30 * 256));
		sheet.setColumnWidth((short) 1, (short) (20 * 256));
		sheet.setColumnWidth((short) 2, (short) (20 * 256));
		short rowNumber = 0;
		short cellNumber = 0;
		this.row = sheet.createRow(rowNumber++);
		if (!headerText.equals("")) {
			this.row.createCell(cellNumber++).setCellValue(headerText);
			rowNumber++;
			this.row = sheet.createRow(rowNumber += 4);
		}
		CalendarMonth currentMonth = new CalendarMonth();
		CalendarMonth previousMonth = currentMonth.getPreviousCalendarMonth();
		int numberOfInvoicesForCurrentMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfInvoicesForCurrentMonth();
		int numberOfInvoicesForPreviousMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfInvoicesForMonth(previousMonth);
		int numberOfChildrenForCurrentMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfChildrenForCurrentMonth();
		int numberOfChildrenForPreviousMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getNumberOfChildrenForMonth(previousMonth);
		int totalInvoiceRecordAmountForCurrentMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getTotalInvoiceRecordAmountForCurrentMonth();
		int totalInvoiceRecordAmountFoPreviousMonth = ((InvoiceHeaderHome) IDOLookup.getHome(InvoiceHeader.class)).getTotalInvoiceRecordAmountForMonth(previousMonth);
		this.row = sheet.createRow(rowNumber++);
		this.row.createCell(cellNumber++).setCellValue("Innevarande månad");
		this.row.createCell(cellNumber).setCellValue("Föregående månad");
		this.row = sheet.createRow(rowNumber++);
		this.row.createCell(cellNumber--).setCellValue(numberOfInvoicesForPreviousMonth);
		this.row.createCell(cellNumber--).setCellValue(numberOfInvoicesForCurrentMonth);
		this.row.createCell(cellNumber).setCellValue("Total antal generade fakturor");
		this.row = sheet.createRow(rowNumber++);
		this.row.createCell(cellNumber++).setCellValue("Total antal behandlade indvider");
		this.row.createCell(cellNumber++).setCellValue(numberOfChildrenForCurrentMonth);
		this.row.createCell(cellNumber).setCellValue(numberOfChildrenForPreviousMonth);
		this.row = sheet.createRow(rowNumber++);
		this.row.createCell(cellNumber--).setCellValue(totalInvoiceRecordAmountFoPreviousMonth);
		this.row.createCell(cellNumber--).setCellValue(totalInvoiceRecordAmountForCurrentMonth);
		this.row.createCell(cellNumber).setCellValue("Totalt fakturerat belopp");
		if (signingFooter) {
			createSigningFooter(sheet, rowNumber);
		}
		saveExcelWorkBook(fileName, this.wb);
	}

	protected void createPaymentSigningFilesExcel(Collection data, String fileName, String headerText)
			throws IOException, FinderException {
		if (data != null && !data.isEmpty()) {
			int[] columnWidths = { 25, 35, 12, 12 };
			String[] columnNames = { "Anordnare", "Text", "Placeringar", "Belopp" };
			createExcelWorkBook(columnWidths, columnNames, headerText);
			HSSFSheet sheet = this.wb.getSheet("Excel");
			short rowNumber = (short) (sheet.getLastRowNum() + 1);
			short cellNumber = 0;
			ArrayList paymentHeaders = new ArrayList(data);
			Collections.sort(paymentHeaders, new PaymentComparator());
			Iterator it = paymentHeaders.iterator();
			boolean firstRecord;
			long recordAmount;
			long totalHeaderAmount = 0;
			long totalAmount = 0;
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
					this.row = sheet.createRow(rowNumber++);
					this.row.createCell(cellNumber++).setCellValue(school.getName());
					while (prIt.hasNext()) {
						PaymentRecord pRec = (PaymentRecord) prIt.next();
						if (!firstRecord) {
							this.row = sheet.createRow(rowNumber++);
						}
						this.row.createCell(cellNumber++).setCellValue(pRec.getPaymentText());
						recordAmount = AccountingUtil.roundAmount(pRec.getTotalAmount());
						totalHeaderAmount += recordAmount;
						totalHeaderStudents += pRec.getPlacements();
						this.cell = this.row.createCell(cellNumber++);
						this.cell.setCellValue(pRec.getPlacements());
						this.cell.setCellStyle((getStyleAlignRight()));
						this.cell = this.row.createCell(cellNumber--);
						this.cell.setCellValue(getNumberFormat().format(recordAmount));
						this.cell.setCellStyle((getStyleAlignRight()));
						cellNumber--;
						if (!prIt.hasNext()) {
							cellNumber--;
							this.row = sheet.createRow(rowNumber++);
							this.row.createCell(cellNumber++).setCellValue("");
							this.row.createCell(cellNumber++).setCellValue("Summa");
							this.row.createCell(cellNumber++).setCellValue(totalHeaderStudents);
							this.row.createCell(cellNumber--).setCellValue(getNumberFormat().format(totalHeaderAmount));
						}
						firstRecord = false;
					}
					cellNumber -= 2;
					totalAmount += totalHeaderAmount;
					totalHeaderAmount = 0;
					totalStudents += totalHeaderStudents;
					totalHeaderStudents = 0;
					for (short i = this.row.getFirstCellNum(); i <= this.row.getLastCellNum(); i++) {
						this.row.getCell(i).setCellStyle(getStyleItalicUnderlineAlignRight());
					}
				}
			}
			this.row = sheet.createRow(rowNumber++);
			this.cell = this.row.createCell(cellNumber += 2);
			this.cell.setCellValue(getNumberFormat().format(totalStudents));
			this.cell.setCellStyle(getStyleBoldAlignRight());
			this.cell = this.row.createCell(cellNumber += 1);
			this.cell.setCellValue(getNumberFormat().format(totalAmount));
			this.cell.setCellStyle(getStyleBoldAlignRight());
			rowNumber++;
			this.row = sheet.createRow(rowNumber++);
			this.cell = this.row.createCell(cellNumber -= 3);
			this.cell.setCellValue("Summa från egna kommunala anordnare");
			this.cell = this.row.createCell(cellNumber += 3);
			this.cell.setCellValue(getNumberFormat().format(getInCommuneSum()));
			this.cell.setCellStyle(getStyleAlignRight());
			this.row = sheet.createRow(rowNumber++);
			this.cell = this.row.createCell(cellNumber -= 3);
			this.cell.setCellValue("Summa från övriga anordnare");
			this.cell = this.row.createCell(cellNumber += 3);
			this.cell.setCellValue(getNumberFormat().format(totalAmount - getInCommuneSum()));
			this.cell.setCellStyle(getStyleAlignRight());
			this.row = sheet.createRow(rowNumber++);
			this.cell = this.row.createCell(cellNumber -= 3);
			this.cell.setCellValue("Bruttosumma att utbetala");
			this.cell.setCellStyle(getStyleBold());
			this.cell = this.row.createCell(cellNumber += 3);
			this.cell.setCellValue(getNumberFormat().format(totalAmount));
			this.cell.setCellStyle(getStyleBoldAlignRight());
			createSigningFooter(sheet, rowNumber);
			saveExcelWorkBook(fileName, this.wb);
		}
	}

	private void createSigningFooter(HSSFSheet sheet, short rowNumber) {
		short cellNumber = 1;
		createStyleBold();
		this.row = sheet.createRow(rowNumber += 4);
		this.cell = this.row.createCell(cellNumber--);
		this.cell.setCellValue("Attestering");
		this.cell.setCellStyle(getStyleBold());
		rowNumber += 4;
		createSigningFooterDetail(sheet, rowNumber, cellNumber, "Granskingsattest");
		rowNumber = createSigningFooterDetail(sheet, rowNumber, cellNumber += 2, "Beslutsattest");
		rowNumber = createSigningFooterDetail(sheet, rowNumber += 5, cellNumber -= 2, "Behörighetsattest");
	}

	private short createSigningFooterDetail(HSSFSheet sheet, short rowNumber, short cellNumber, String text) {
		this.row = sheet.createRow(rowNumber);
		this.cell = this.row.createCell(cellNumber);
		this.cell.setCellValue(text);
		this.row = sheet.createRow(rowNumber += 2);
		this.cell = this.row.createCell(cellNumber);
		this.cell.setCellValue("Datum...............................");
		this.row = sheet.createRow(rowNumber += 2);
		this.cell = this.row.createCell(cellNumber);
		this.cell.setCellValue("...........................................");
		return rowNumber;
	}

	private void createExcelWorkBook(int[] columnWidths, String[] columnNames, String headerText) {
		this.wb = new HSSFWorkbook();
		createStyleBoldUnderlineAlignRight();
		createStyleBoldUnderline();
		HSSFSheet sheet = this.wb.createSheet("Excel");
		for (short i = 0; i < columnWidths.length; i++) {
			sheet.setColumnWidth(i, (short) (columnWidths[i] * 256));
		}
		short rowNumber = 0;
		this.row = sheet.createRow(rowNumber++);
		if (!headerText.equals("")) {
			this.row.createCell((short) 0).setCellValue(headerText);
			rowNumber++;
			this.row = sheet.createRow(rowNumber++);
		}
		for (short i = 0; i < columnNames.length; i++) {
			this.cell = this.row.createCell(i);
			this.cell.setCellValue(columnNames[i]);
			if (columnNames[i].equals("Belopp")) {
				this.cell.setCellStyle(getStyleBoldUnderlineAlignRight());
			}
			else {
				this.cell.setCellStyle(getStyleBoldUnderline());
			}
		}
	}

	private void saveExcelWorkBook(String fileName, HSSFWorkbook wb) throws IOException {
		FileOutputStream out = new FileOutputStream(fileName);
		wb.write(out);
		out.close();
	}

	private void createNumberFormat() {
		this.numberFormat = NumberFormat.getInstance(Locale.FRENCH);
		this.numberFormat.setMaximumFractionDigits(0);
		this.numberFormat.setMinimumIntegerDigits(1);
	}

	private NumberFormat getNumberFormat() {
		return this.numberFormat;
	}

	private String getDeviationString() {
		return this.deviationString;
	}

	private void setDeviationString(String _deviationString) {
		this.deviationString = _deviationString;
	}

	private HSSFCellStyle getStyleAlignRight() {
		return this.styleAlignRight;
	}

	private HSSFCellStyle getStyleBold() {
		return this.styleBold;
	}

	private HSSFCellStyle getStyleBoldAlignRight() {
		return this.styleBoldAlignRight;
	}

	private HSSFCellStyle getStyleBoldUnderline() {
		return this.styleBoldUnderline;
	}

	private HSSFCellStyle getStyleBoldUnderlineAlignRight() {
		return this.styleBoldUnderlineAlignRight;
	}

	private HSSFCellStyle getStyleItalicUnderlineAlignRight() {
		return this.styleItalicUnderlineAlignRight;
	}

	private HSSFCellStyle createStyleAlignRight() {
		this.styleAlignRight = this.wb.createCellStyle();
		this.styleAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		return this.styleAlignRight;
	}

	private HSSFCellStyle createStyleBold() {
		HSSFFont font = this.wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		this.styleBold = this.wb.createCellStyle();
		this.styleBold.setFont(font);
		return this.styleBold;
	}

	private HSSFCellStyle createStyleBoldAlignRight() {
		HSSFFont font = this.wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		this.styleBoldAlignRight = this.wb.createCellStyle();
		this.styleBoldAlignRight.setFont(font);
		this.styleBoldAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		return this.styleBoldAlignRight;
	}

	private HSSFCellStyle createStyleBoldUnderline() {
		HSSFFont font = this.wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		this.styleBoldUnderline = this.wb.createCellStyle();
		this.styleBoldUnderline.setFont(font);
		this.styleBoldUnderline.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		return this.styleBoldUnderline;
	}

	private HSSFCellStyle createStyleBoldUnderlineAlignRight() {
		HSSFFont font = this.wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		this.styleBoldUnderlineAlignRight = this.wb.createCellStyle();
		this.styleBoldUnderlineAlignRight.setFont(font);
		this.styleBoldUnderlineAlignRight.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		this.styleBoldUnderlineAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		return this.styleBoldUnderlineAlignRight;
	}

	private HSSFCellStyle createStyleItalicUnderlineAlignRight() {
		HSSFFont italicFont = this.wb.createFont();
		italicFont.setItalic(true);
		this.styleItalicUnderlineAlignRight = this.wb.createCellStyle();
		this.styleItalicUnderlineAlignRight.setFont(italicFont);
		this.styleItalicUnderlineAlignRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		this.styleItalicUnderlineAlignRight.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		return this.styleItalicUnderlineAlignRight;
	}

	private long getInCommuneSum() {
		return this.inCommuneSum;
	}

	private void setInCommuneSum(long inCommuneSum) {
		this.inCommuneSum = inCommuneSum;
	}
	
	private IFSBusiness getIFSBusiness() {
		try {
			return (IFSBusiness) IBOLookup.getServiceInstance(this.iwac, IFSBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}
}