/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package bse_oop2_2025;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.sql.*;
import java.time.LocalDate;
import java.awt.Desktop;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author jonah
 */
public class Reports extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Reports.class.getName());

    /**
     * Creates new form Reports
     */
    public Reports() {
        initComponents();
        setupReportTypes();

    }

    private void setupReportTypes() {
        cmbreporttype.removeAllItems(); // Replace with your combo box name
        cmbreporttype.addItem("Select Report Type");
        cmbreporttype.addItem("Daily Revenue Report");
        cmbreporttype.addItem("All Vendors Report");
        cmbreporttype.addItem("All Stalls Report");
        cmbreporttype.addItem("Outstanding Fees Report");
        cmbreporttype.addItem("Payment History Report");
        cmbreporttype.addItem("Complaints Report");

    }

    public void generateVendorReport() {
        try {
            Document document = new Document(PageSize.A4);
            String fileName = "Vendor_Report_" + LocalDate.now() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            // Header
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE);
            Paragraph title = new Paragraph("VENDOR LIST REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDate.now());
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            PdfPCell[] headers = {
                new PdfPCell(new Phrase("ID", headerFont)),
                new PdfPCell(new Phrase("First Name", headerFont)),
                new PdfPCell(new Phrase("Last Name", headerFont)),
                new PdfPCell(new Phrase("Phone", headerFont)),
                new PdfPCell(new Phrase("Type", headerFont))
            };

            for (PdfPCell header : headers) {
                header.setBackgroundColor(BaseColor.DARK_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            // Data
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT vendor_id, first_name, last_name, phone, vendor_type FROM vendors WHERE status = 'Active'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("vendor_id")));
                table.addCell(rs.getString("first_name"));
                table.addCell(rs.getString("last_name"));
                table.addCell(rs.getString("phone"));
                table.addCell(rs.getString("vendor_type"));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            Paragraph footer = new Paragraph("Market Vendor Management System",
                    FontFactory.getFont(FontFactory.TIMES, 8));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            conn.close();

            openPDF(fileName);
            JOptionPane.showMessageDialog(null, "Vendor report generated: " + fileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating vendor report: " + e.getMessage());
        }
    }

    public void generateDailyRevenueReport() {
        try {
            Document document = new Document(PageSize.A4);
            String fileName = "Daily_Revenue_" + LocalDate.now() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE);
            Paragraph title = new Paragraph("DAILY REVENUE REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Date: " + LocalDate.now());
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Revenue summary
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT "
                    + "COUNT(*) as total_fees, "
                    + "SUM(CASE WHEN payment_status = 'Paid' THEN amount ELSE 0 END) as collected, "
                    + "SUM(CASE WHEN payment_status = 'Pending' THEN amount ELSE 0 END) as pending "
                    + "FROM daily_fees WHERE fee_date = CURDATE()";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(60);
                summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

                com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

                PdfPCell labelCell = new PdfPCell(new Phrase("Total Fees Generated:", boldFont));
                labelCell.setBorder(Rectangle.NO_BORDER);
                summaryTable.addCell(labelCell);
                summaryTable.addCell(String.valueOf(rs.getInt("total_fees")));

                labelCell = new PdfPCell(new Phrase("Amount Collected:", boldFont));
                labelCell.setBorder(Rectangle.NO_BORDER);
                summaryTable.addCell(labelCell);
                summaryTable.addCell("UGX " + String.format("%.2f", rs.getDouble("collected")));

                labelCell = new PdfPCell(new Phrase("Amount Pending:", boldFont));
                labelCell.setBorder(Rectangle.NO_BORDER);
                summaryTable.addCell(labelCell);
                summaryTable.addCell("UGX " + String.format("%.2f", rs.getDouble("pending")));

                double total = rs.getDouble("collected") + rs.getDouble("pending");
                if (total > 0) {
                    double rate = (rs.getDouble("collected") / total) * 100;
                    labelCell = new PdfPCell(new Phrase("Collection Rate:", boldFont));
                    labelCell.setBorder(Rectangle.NO_BORDER);
                    summaryTable.addCell(labelCell);
                    summaryTable.addCell(String.format("%.1f%%", rate));
                }

                document.add(summaryTable);
            }

            document.close();
            conn.close();

            openPDF(fileName);
            JOptionPane.showMessageDialog(null, "Revenue report generated: " + fileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating revenue report: " + e.getMessage());
        }
    }

    public void generateOutstandingFeesReport() {
        try {
            Document document = new Document(PageSize.A4);
            String fileName = "Outstanding_Fees_" + LocalDate.now() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.RED);
            Paragraph title = new Paragraph("OUTSTANDING FEES REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDate.now());
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Outstanding fees table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            // Headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            String[] headerTexts = {"Vendor Name", "Amount", "Fee Date", "Days Overdue"};

            for (String headerText : headerTexts) {
                PdfPCell header = new PdfPCell(new Phrase(headerText, headerFont));
                header.setBackgroundColor(BaseColor.DARK_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            // Data
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT CONCAT(v.first_name, ' ', v.last_name) AS vendor_name, "
                    + "f.amount, f.fee_date, DATEDIFF(CURDATE(), f.fee_date) AS days_overdue "
                    + "FROM daily_fees f "
                    + "JOIN vendors v ON f.vendor_id = v.vendor_id "
                    + "WHERE f.payment_status = 'Pending' "
                    + "ORDER BY f.fee_date";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            double totalOutstanding = 0;
            while (rs.next()) {
                table.addCell(rs.getString("vendor_name"));
                double amount = rs.getDouble("amount");
                table.addCell("UGX " + String.format("%.2f", amount));
                table.addCell(rs.getDate("fee_date").toString());
                table.addCell(String.valueOf(rs.getInt("days_overdue")));
                totalOutstanding += amount;
            }

            document.add(table);
            document.add(new Paragraph(" "));

            com.itextpdf.text.Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED);
            Paragraph total = new Paragraph("TOTAL OUTSTANDING: UGX " + String.format("%.2f", totalOutstanding), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            conn.close();

            openPDF(fileName);
            JOptionPane.showMessageDialog(null, "Outstanding fees report generated: " + fileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating outstanding fees report: " + e.getMessage());
        }
    }

    public void generateStallsReport() {
        try {
            Document document = new Document(PageSize.A4.rotate()); // Landscape
            String fileName = "Stalls_Report_" + LocalDate.now() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE);
            Paragraph title = new Paragraph("MARKET STALLS REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDate.now());
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Stalls table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            String[] headerTexts = {"Stall No.", "Section", "Monthly Rent", "Status", "Current Vendor", "Assignment Date"};

            for (String headerText : headerTexts) {
                PdfPCell header = new PdfPCell(new Phrase(headerText, headerFont));
                header.setBackgroundColor(BaseColor.DARK_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            // Data
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT s.stall_number, s.section, s.monthly_rent, s.status, "
                    + "CONCAT(IFNULL(v.first_name, ''), ' ', IFNULL(v.last_name, '')) AS vendor_name, "
                    + "sa.assignment_date "
                    + "FROM stalls s "
                    + "LEFT JOIN stall_assignments sa ON s.stall_id = sa.stall_id AND sa.status = 'Active' " + "LEFT JOIN vendors v ON sa.vendor_id = v.vendor_id "
                    + "ORDER BY s.stall_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                table.addCell(rs.getString("stall_number"));
                table.addCell(rs.getString("section"));
                table.addCell("UGX " + String.format("%.2f", rs.getDouble("monthly_rent")));
                table.addCell(rs.getString("status"));
                String vendorName = rs.getString("vendor_name");
                table.addCell(vendorName != null && !vendorName.trim().isEmpty() ? vendorName : "N/A");
                Date assignmentDate = rs.getDate("assignment_date");
                table.addCell(assignmentDate != null ? assignmentDate.toString() : "N/A");
            }

            document.add(table);
            document.close();
            conn.close();

            openPDF(fileName);
            JOptionPane.showMessageDialog(null, "Stalls report generated: " + fileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating stalls report: " + e.getMessage());
        }
    }

    public void generateComplaintsReport() {
        try {
            Document document = new Document(PageSize.A4);
            String fileName = "Complaints_Report_" + LocalDate.now() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.RED);
            Paragraph title = new Paragraph("COMPLAINTS REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDate.now());
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Summary section
            Connection conn = DbConnection.getConnection();
            String summarySQL = "SELECT "
                    + "COUNT(*) as total_complaints, "
                    + "SUM(CASE WHEN status = 'Open' THEN 1 ELSE 0 END) as open_complaints, "
                    + "SUM(CASE WHEN status = 'Resolved' THEN 1 ELSE 0 END) as resolved_complaints "
                    + "FROM complaints";
            Statement summaryStmt = conn.createStatement();
            ResultSet summaryRs = summaryStmt.executeQuery(summarySQL);

            if (summaryRs.next()) {
                com.itextpdf.text.Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                document.add(new Paragraph("SUMMARY:", summaryFont));
                document.add(new Paragraph("Total Complaints: " + summaryRs.getInt("total_complaints")));
                document.add(new Paragraph("Open Complaints: " + summaryRs.getInt("open_complaints")));
                document.add(new Paragraph("Resolved Complaints: " + summaryRs.getInt("resolved_complaints")));
                document.add(new Paragraph(" "));
            }

            // Complaints table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Set column widths
            float[] columnWidths = {1f, 2f, 3f, 1.5f, 1.5f};
            table.setWidths(columnWidths);

            // Headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            String[] headerTexts = {"ID", "Vendor Name", "Description", "Date", "Status"};

            for (String headerText : headerTexts) {
                PdfPCell header = new PdfPCell(new Phrase(headerText, headerFont));
                header.setBackgroundColor(BaseColor.DARK_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            // Data
            String sql = "SELECT c.complaint_id, CONCAT(v.first_name, ' ', v.last_name) AS vendor_name, "
                    + "c.description, c.complaint_date, c.status "
                    + "FROM complaints c "
                    + "JOIN vendors v ON c.vendor_id = v.vendor_id "
                    + "ORDER BY c.complaint_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("complaint_id")));
                table.addCell(rs.getString("vendor_name"));

                // Truncate description if too long
                String description = rs.getString("description");
                if (description.length() > 100) {
                    description = description.substring(0, 97) + "...";
                }
                table.addCell(description);

                table.addCell(rs.getDate("complaint_date").toString());

                // Color code status
                PdfPCell statusCell = new PdfPCell(new Phrase(rs.getString("status")));
                String status = rs.getString("status");
                if ("Open".equals(status)) {
                    statusCell.setBackgroundColor(BaseColor.YELLOW);
                } else if ("Resolved".equals(status)) {
                    statusCell.setBackgroundColor(BaseColor.GREEN);
                } else if ("Investigating".equals(status)) {
                    statusCell.setBackgroundColor(BaseColor.ORANGE);
                }
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(statusCell);
            }

            document.add(table);
            document.close();
            conn.close();

            openPDF(fileName);
            JOptionPane.showMessageDialog(null, "Complaints report generated: " + fileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating complaints report: " + e.getMessage());
        }
    }

    public void generatePaymentHistoryReport() {
        try {
            Document document = new Document(PageSize.A4);
            String fileName = "Payment_History_" + LocalDate.now() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.GREEN);
            Paragraph title = new Paragraph("PAYMENT HISTORY REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDate.now());
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Payments table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            // Headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            String[] headerTexts = {"Vendor Name", "Amount", "Payment Date", "Method"};

            for (String headerText : headerTexts) {
                PdfPCell header = new PdfPCell(new Phrase(headerText, headerFont));
                header.setBackgroundColor(BaseColor.DARK_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            // Data
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT CONCAT(v.first_name, ' ', v.last_name) AS vendor_name, "
                    + "p.amount_paid, p.payment_date, p.payment_method "
                    + "FROM payments p "
                    + "JOIN daily_fees f ON p.fee_id = f.fee_id "
                    + "JOIN vendors v ON f.vendor_id = v.vendor_id "
                    + "ORDER BY p.payment_date DESC LIMIT 50";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            double totalPayments = 0;
            while (rs.next()) {
                table.addCell(rs.getString("vendor_name"));
                double amount = rs.getDouble("amount_paid");
                table.addCell("UGX " + String.format("%.2f", amount));
                table.addCell(rs.getDate("payment_date").toString());
                table.addCell(rs.getString("payment_method"));
                totalPayments += amount;
            }

            document.add(table);
            document.add(new Paragraph(" "));

            com.itextpdf.text.Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.GREEN);
            Paragraph total = new Paragraph("TOTAL PAYMENTS SHOWN: UGX " + String.format("%.2f", totalPayments), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            conn.close();

            openPDF(fileName);
            JOptionPane.showMessageDialog(null, "Payment history report generated: " + fileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating payment history report: " + e.getMessage());
        }
    }

    // Helper method to open PDF
    private void openPDF(String fileName) {
        if (Desktop.isDesktopSupported()) {
            try {
                File pdfFile = new File(fileName);
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Could not open PDF file: " + ex.getMessage());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbreporttype = new javax.swing.JLabel();
        cmbreporttype = new javax.swing.JComboBox<>();
        lbstartdate = new javax.swing.JLabel();
        cndstartdate = new com.toedter.calendar.JCalendar();
        lbenddate = new javax.swing.JLabel();
        cndenddate = new com.toedter.calendar.JCalendar();
        btngenerate = new javax.swing.JButton();
        lbreport = new javax.swing.JLabel();
        btnexport = new javax.swing.JButton();
        btnback = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtReport = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lbreporttype.setText("Report Type");

        cmbreporttype.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbreporttype.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbreporttypeActionPerformed(evt);
            }
        });

        lbstartdate.setText("start date");

        lbenddate.setText("End Date");

        btngenerate.setText("Generate Report");
        btngenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btngenerateActionPerformed(evt);
            }
        });

        lbreport.setText("Report");

        btnexport.setText("Export");
        btnexport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnexportActionPerformed(evt);
            }
        });

        btnback.setText("Back");
        btnback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbackActionPerformed(evt);
            }
        });

        txtReport.setColumns(20);
        txtReport.setRows(5);
        jScrollPane2.setViewportView(txtReport);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbreporttype)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lbreport)
                        .addComponent(lbstartdate)))
                .addGap(69, 69, 69)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cndstartdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lbenddate)
                                .addGap(62, 62, 62)
                                .addComponent(cndenddate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnexport)))
                        .addGap(59, 59, 59))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbreporttype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(608, 608, 608)
                                .addComponent(btnback)))
                        .addContainerGap(59, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btngenerate)
                .addGap(410, 410, 410))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbreporttype)
                    .addComponent(cmbreporttype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(lbstartdate))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cndenddate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cndstartdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbreport)
                        .addGap(188, 188, 188))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(btnexport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                        .addComponent(btnback)
                        .addGap(37, 37, 37))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(114, 114, 114)
                .addComponent(lbenddate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btngenerate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(129, 129, 129))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbreporttypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbreporttypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbreporttypeActionPerformed

    private void btnexportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportActionPerformed
        // TODO add your handling code here:
        String reportContent = txtReport.getText();
        if (reportContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please generate a report first!");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Report content:\n\n" + reportContent.substring(0, Math.min(200, reportContent.length())) + "...\n\n"
                + "In a real system, this would be saved to a file.",
                "Export Report",
                JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnexportActionPerformed

    private void btnbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbackActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        new MainDashBoard().setVisible(true);

    }//GEN-LAST:event_btnbackActionPerformed

    private void btngenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btngenerateActionPerformed
        // TODO add your handling code here:
        if (cmbreporttype.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a report type!");
            return;
        }

        String reportType = cmbreporttype.getSelectedItem().toString();
        Reports reports = new Reports();

        switch (reportType) {
            case "All Vendors Report":
                reports.generateVendorReport();
                break;
            case "Daily Revenue Report":
                reports.generateDailyRevenueReport();
                break;
            case "Outstanding Fees Report":
                reports.generateOutstandingFeesReport();
                break;
            case "All Stalls Report":
                reports.generateStallsReport();
                break;
            case "Complaints Report":
                reports.generateComplaintsReport();
                break;
            case "Payment History Report":
                reports.generatePaymentHistoryReport();
                break;
            default:
                JOptionPane.showMessageDialog(this, "PDF generation not available for this report type.");
        }

    }//GEN-LAST:event_btngenerateActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Reports().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnback;
    private javax.swing.JButton btnexport;
    private javax.swing.JButton btngenerate;
    private javax.swing.JComboBox<String> cmbreporttype;
    private com.toedter.calendar.JCalendar cndenddate;
    private com.toedter.calendar.JCalendar cndstartdate;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbenddate;
    private javax.swing.JLabel lbreport;
    private javax.swing.JLabel lbreporttype;
    private javax.swing.JLabel lbstartdate;
    private javax.swing.JTextArea txtReport;
    // End of variables declaration//GEN-END:variables

}
