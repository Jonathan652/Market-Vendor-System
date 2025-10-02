/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package bse_oop2_2025;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.sql.*;
import java.time.LocalDate;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
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
        setLocationRelativeTo(null);
        setTitle("Reports Form");
        setupReportTypes();
        
        

    }

    private void setupReportTypes() {
        cmbreporttype.removeAllItems(); 
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
        Document doc = new Document();
        String fileName = "Vendor_List_" + LocalDate.now() + ".pdf";
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        doc.add(new Paragraph("VENDOR LIST"));
        doc.add(new Paragraph("Date: " + LocalDate.now()));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.addCell("ID");
        table.addCell("Name");
        table.addCell("Phone");
        table.addCell("Type");

        Connection conn = DbConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT vendor_id, first_name, last_name, phone, vendor_type FROM vendors"
        );

        while (rs.next()) {
            table.addCell(rs.getString("vendor_id"));
            table.addCell(rs.getString("first_name") + " " + rs.getString("last_name"));
            table.addCell(rs.getString("phone"));
            table.addCell(rs.getString("vendor_type"));
        }

        doc.add(table);
        doc.close();
        conn.close();
        
        openPDF(fileName);
        JOptionPane.showMessageDialog(null, "Vendor report opened!");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}

    public void generateDailyRevenueReport() {
        try {
        Document doc = new Document();
        String fileName = "Daily_Revenue_" + LocalDate.now() + ".pdf";
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        doc.add(new Paragraph("DAILY REVENUE - " + LocalDate.now()));
        doc.add(new Paragraph(" "));

        Connection conn = DbConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT SUM(amount) as total FROM daily_fees WHERE fee_date = CURDATE()"
        );

        if (rs.next()) {
            double total = rs.getDouble("total");
            doc.add(new Paragraph("Total Revenue: UGX " + total));
        }

        doc.close();
        conn.close();
        
        openPDF(fileName);
        JOptionPane.showMessageDialog(null, "Revenue report opened!");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}
    public void generateOutstandingFeesReport() {
         try {
        Document doc = new Document();
        String fileName = "Unpaid_Fees_" + LocalDate.now() + ".pdf";
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        doc.add(new Paragraph("UNPAID FEES REPORT"));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.addCell("Vendor");
        table.addCell("Amount");
        table.addCell("Due Date");

        Connection conn = DbConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT v.first_name, v.last_name, f.amount, f.fee_date " +
            "FROM daily_fees f JOIN vendors v ON f.vendor_id = v.vendor_id " +
            "WHERE f.payment_status = 'Pending'"
        );

        double total = 0;
        while (rs.next()) {
            table.addCell(rs.getString("first_name") + " " + rs.getString("last_name"));
            double amount = rs.getDouble("amount");
            table.addCell("UGX " + amount);
            table.addCell(rs.getDate("fee_date").toString());
            total += amount;
        }

        doc.add(table);
        doc.add(new Paragraph("Total Unpaid: UGX " + total));
        doc.close();
        conn.close();
        
        openPDF(fileName);
        JOptionPane.showMessageDialog(null, "Unpaid fees report opened!");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}
    public void generateStallsReport() {
         try {
        Document doc = new Document();
        String fileName = "Stalls_" + LocalDate.now() + ".pdf";
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        doc.add(new Paragraph("MARKET STALLS"));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.addCell("Stall No");
        table.addCell("Section");
        table.addCell("Status");

        Connection conn = DbConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT stall_number, section, status FROM stalls ORDER BY stall_number"
        );

        while (rs.next()) {
            table.addCell(rs.getString("stall_number"));
            table.addCell(rs.getString("section"));
            table.addCell(rs.getString("status"));
        }

        doc.add(table);
        doc.close();
        conn.close();
        
        openPDF(fileName);
        JOptionPane.showMessageDialog(null, "Stalls report opened!");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}
    public void generateComplaintsReport() {
        try {
        Document doc = new Document();
        String fileName = "Complaints_" + LocalDate.now() + ".pdf";
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        doc.add(new Paragraph("COMPLAINTS REPORT"));
        doc.add(new Paragraph("Date: " + LocalDate.now()));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.addCell("Vendor");
        table.addCell("Complaint");
        table.addCell("Date");
        table.addCell("Status");

        Connection conn = DbConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT v.first_name, v.last_name, c.description, c.complaint_date, c.status " +
            "FROM complaints c JOIN vendors v ON c.vendor_id = v.vendor_id " +
            "ORDER BY c.complaint_date DESC"
        );

        while (rs.next()) {
            table.addCell(rs.getString("first_name") + " " + rs.getString("last_name"));
            
            String complaint = rs.getString("description");
            if (complaint.length() > 50) {
                complaint = complaint.substring(0, 47) + "...";
            }
            table.addCell(complaint);
            
            table.addCell(rs.getDate("complaint_date").toString());
            table.addCell(rs.getString("status"));
        }

        doc.add(table);
        doc.close();
        conn.close();
        
        openPDF(fileName);
        JOptionPane.showMessageDialog(null, "Complaints report opened!");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}
    public void generatePaymentHistoryReport() {
         try {
        Document doc = new Document();
        String fileName = "Payments_" + LocalDate.now() + ".pdf";
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        doc.add(new Paragraph("PAYMENT HISTORY"));
        doc.add(new Paragraph("Date: " + LocalDate.now()));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.addCell("Vendor");
        table.addCell("Amount");
        table.addCell("Date");
        table.addCell("Method");

        Connection conn = DbConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT v.first_name, v.last_name, p.amount_paid, p.payment_date, p.payment_method " +
            "FROM payments p " +
            "JOIN daily_fees f ON p.fee_id = f.fee_id " +
            "JOIN vendors v ON f.vendor_id = v.vendor_id " +
            "ORDER BY p.payment_date DESC LIMIT 30"
        );

        double total = 0;
        while (rs.next()) {
            table.addCell(rs.getString("first_name") + " " + rs.getString("last_name"));
            double amount = rs.getDouble("amount_paid");
            table.addCell("UGX " + amount);
            table.addCell(rs.getDate("payment_date").toString());
            table.addCell(rs.getString("payment_method"));
            total += amount;
        }

        doc.add(table);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Total Shown: UGX " + total));
        doc.close();
        conn.close();
        
        openPDF(fileName);
        JOptionPane.showMessageDialog(null, "Payment history opened!");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}

    // Helper method to open PDF
    private void openPDF(String fileName) {
    try {
        File pdfFile = new File(fileName);
        if (pdfFile.exists()) {
            Desktop.getDesktop().open(pdfFile);
        } else {
            JOptionPane.showMessageDialog(null, "PDF file not found: " + fileName);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Could not open PDF: " + e.getMessage());
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
        btnexport = new javax.swing.JButton();
        btnback = new javax.swing.JButton();

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbreporttype)
                    .addComponent(lbstartdate))
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
                                .addGap(0, 0, Short.MAX_VALUE)
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
                .addGap(135, 135, 135)
                .addComponent(btnexport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addComponent(btnback)
                .addGap(37, 37, 37))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(114, 114, 114)
                .addComponent(lbenddate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btngenerate)
                .addGap(231, 231, 231))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbreporttypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbreporttypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbreporttypeActionPerformed

    private void btnexportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexportActionPerformed
        // TODO add your handling code here:
       JOptionPane.showMessageDialog(this, 
        "Reports are automatically generated as PDF files and opened immediately.\n" +
        "No additional export needed!\n\n" +
        "Check your project folder for the PDF files.");

    }//GEN-LAST:event_btnexportActionPerformed

    private void btnbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbackActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        new MainDashBoard().setVisible(true);

    }//GEN-LAST:event_btnbackActionPerformed

    private void btngenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btngenerateActionPerformed
        // TODO add your handling code here:
        String reportType = cmbreporttype.getSelectedItem().toString();
    
    if (reportType.equals("Select Report Type")) {
        JOptionPane.showMessageDialog(this, "Please choose a report type!");
        return;
    }

    switch (reportType) {
        case "Daily Revenue Report":
            generateDailyRevenueReport();
            break;
        case "All Vendors Report":
            generateVendorReport();
            break;
        case "All Stalls Report":
            generateStallsReport();
            break;
        case "Outstanding Fees Report":
            generateOutstandingFeesReport();
            break;
        case "Payment History Report":
            generatePaymentHistoryReport();
            break;
        case "Complaints Report":
            generateComplaintsReport();
            break;
        default:
            JOptionPane.showMessageDialog(this, "That report isn't ready yet!");
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
    private javax.swing.JLabel lbenddate;
    private javax.swing.JLabel lbreporttype;
    private javax.swing.JLabel lbstartdate;
    // End of variables declaration//GEN-END:variables

}
