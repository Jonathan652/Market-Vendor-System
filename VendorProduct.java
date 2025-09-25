/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package bse_oop2_2025;

import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;

/**
 *
 * @author jonah
 */

public class VendorProduct extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VendorProduct.class.getName());

    /**
     * Creates new form VendorProduct
     */
    public VendorProduct() {
        initComponents();
        loadVendors();
        loadAllProducts();

    }
    private int currentVendorId = -1;
    public void loadVendors() {
    try {
        Connection conn = DbConnection.getConnection();
        String sql = "SELECT vendor_id, CONCAT(first_name, ' ', last_name) AS vendor_name FROM vendors WHERE status = 'Active'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        cmbvendor.removeAllItems(); 
        cmbvendor.addItem("Select Vendor");
        
        while (rs.next()) {
            String item = rs.getInt("vendor_id") + " - " + rs.getString("vendor_name");
            cmbvendor.addItem(item);
        }
        
        conn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading vendors: " + e.getMessage());
    }
}
    public void loadAllProducts() {
    try {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT product_id, product_name, category FROM products ORDER BY category, product_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            DefaultListModel<String> model = new DefaultListModel<>();
            
            while (rs.next()) {
                String productInfo = rs.getInt("product_id") + " - " +
                        rs.getString("product_name") + " (" +
                        rs.getString("category") + ")";
                model.addElement(productInfo);
            }
            
            lstavailable.setModel(model);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
    }
    }
    public void loadAvailableProducts() {
    if (currentVendorId == -1) return;
    
    try {
        Connection conn = DbConnection.getConnection();
        String sql = "SELECT p.product_id, p.product_name, p.category " +
                    "FROM products p " +
                    "WHERE p.product_id NOT IN (" +
                    "    SELECT vp.product_id FROM vendor_products vp " +
                    "    WHERE vp.vendor_id = ? AND vp.status = 'Active'" +
                    ") ORDER BY p.category, p.product_name";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, currentVendorId);
        ResultSet rs = pst.executeQuery();
        
        DefaultListModel<String> model = new DefaultListModel<>();
        
        while (rs.next()) {
            String productInfo = rs.getInt("product_id") + " - " + 
                               rs.getString("product_name") + " (" + 
                               rs.getString("category") + ")";
            model.addElement(productInfo);
        }
        
        lstavailable.setModel(model); 
        
        conn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading available products: " + e.getMessage());
    }
}
    public void loadVendorProducts() {
    if (currentVendorId == -1) return;
    
    try {
        Connection conn = DbConnection.getConnection();
        String sql = "SELECT p.product_id, p.product_name, p.category, vp.selling_price " +
                    "FROM vendor_products vp " +
                    "JOIN products p ON vp.product_id = p.product_id " +
                    "WHERE vp.vendor_id = ? AND vp.status = 'Active' " +
                    "ORDER BY p.category, p.product_name";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, currentVendorId);
        ResultSet rs = pst.executeQuery();
        
        DefaultListModel<String> model = new DefaultListModel<>();
        
        while (rs.next()) {
            String productInfo = rs.getInt("product_id") + " - " + 
                               rs.getString("product_name") + " (" + 
                               rs.getString("category") + ")" +
                               " - UGX " + rs.getDouble("selling_price");
            model.addElement(productInfo);
        }
        
        lstassigned.setModel(model);
        
        conn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading vendor products: " + e.getMessage());
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

        lbvendor = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lbavailable = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstavailable = new javax.swing.JList<>();
        lbassigned = new javax.swing.JLabel();
        btnaddproduct = new javax.swing.JButton();
        btnremoveproduct = new javax.swing.JButton();
        btnsave = new javax.swing.JButton();
        cmbvendor = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstassigned = new javax.swing.JList<>();
        btnback = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lbvendor.setText("Vendor");

        lbavailable.setText("Available");

        lstavailable.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstavailable);

        lbassigned.setText("Assigned");

        btnaddproduct.setText("Add Product");
        btnaddproduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnaddproductActionPerformed(evt);
            }
        });

        btnremoveproduct.setText("Remove Product");
        btnremoveproduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnremoveproductActionPerformed(evt);
            }
        });

        btnsave.setText("Save");
        btnsave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnsaveActionPerformed(evt);
            }
        });

        cmbvendor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbvendor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbvendorActionPerformed(evt);
            }
        });

        lstassigned.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(lstassigned);

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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbassigned)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(73, 73, 73))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbavailable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(105, 105, 105)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbvendor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbvendor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnback)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnaddproduct)
                                .addGap(34, 34, 34)
                                .addComponent(btnremoveproduct)
                                .addGap(18, 18, 18)
                                .addComponent(btnsave)))
                        .addGap(0, 20, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(lbvendor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbvendor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbavailable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lbassigned)
                        .addGap(28, 28, 28))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnaddproduct)
                    .addComponent(btnremoveproduct)
                    .addComponent(btnsave))
                .addGap(18, 18, 18)
                .addComponent(btnback)
                .addGap(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbvendorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbvendorActionPerformed
        // TODO add your handling code here:
        if (cmbvendor.getSelectedIndex() > 0) {
            String vendorInfo = cmbvendor.getSelectedItem().toString();
            currentVendorId = Integer.parseInt(vendorInfo.split(" - ")[0]);

            loadVendorProducts();
            loadAvailableProducts();
        } else {
            currentVendorId = -1;
            clearLists();
        }

    }//GEN-LAST:event_cmbvendorActionPerformed

    private void btnremoveproductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnremoveproductActionPerformed
        // TODO add your handling code here:
        if (currentVendorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vendor first!");
            return;
        }

        String selectedProduct = lstassigned.getSelectedValue(); // Replace with your assigned list name
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to remove!");
            return;
        }

        // Extract product ID
        int productId = Integer.parseInt(selectedProduct.split(" - ")[0]);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this product from the vendor?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                try (Connection conn = DbConnection.getConnection()) {
                    String sql = "UPDATE vendor_products SET status = 'Inactive' WHERE vendor_id = ? AND product_id = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setInt(1, currentVendorId);
                    pst.setInt(2, productId);

                    int result = pst.executeUpdate();

                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "Product removed successfully!");
                        loadVendorProducts();
                        loadAvailableProducts();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove product!");
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error removing product: " + e.getMessage());
            }
        }

    }//GEN-LAST:event_btnremoveproductActionPerformed

    private void btnsaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsaveActionPerformed
        // TODO add your handling code here:
        if (currentVendorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vendor first!");
            return;
        }

        JOptionPane.showMessageDialog(this, """
                                        All changes have been saved automatically!
                                        Vendor's product assignments are up to date.""",
                "Save Complete",
                JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnsaveActionPerformed

    private void btnaddproductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnaddproductActionPerformed
        // TODO add your handling code here:
        if (currentVendorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vendor first!");
            return;
        }

        String selectedProduct = lstavailable.getSelectedValue();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to add!");
            return;
        }

        int productId = Integer.parseInt(selectedProduct.split(" - ")[0]);

        String priceStr = JOptionPane.showInputDialog(this,
                "Enter selling price for this product:",
                "Set Selling Price",
                JOptionPane.QUESTION_MESSAGE);

        if (priceStr == null || priceStr.trim().isEmpty()) {
            return;
        }

        try {
            double sellingPrice = Double.parseDouble(priceStr);

            if (sellingPrice <= 0) {
                JOptionPane.showMessageDialog(this, "Selling price must be greater than 0!");
                return;
            }

            Connection conn = DbConnection.getConnection();
            String sql = "INSERT INTO vendor_products (vendor_id, product_id, selling_price, date_authorized, status) VALUES (?, ?, ?, CURDATE(), 'Active')";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, currentVendorId);
            pst.setInt(2, productId);
            pst.setDouble(3, sellingPrice);

            int result = pst.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                loadVendorProducts();
                loadAvailableProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product!");
            }

            conn.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage());
        }

    }//GEN-LAST:event_btnaddproductActionPerformed

    private void btnbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbackActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        new Vendor_management().setVisible(true);
    
    }//GEN-LAST:event_btnbackActionPerformed

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
    java.awt.EventQueue.invokeLater(() -> new VendorProduct().setVisible(true));
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnaddproduct;
    private javax.swing.JButton btnback;
    private javax.swing.JButton btnremoveproduct;
    private javax.swing.JButton btnsave;
    private javax.swing.JComboBox<String> cmbvendor;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbassigned;
    private javax.swing.JLabel lbavailable;
    private javax.swing.JLabel lbvendor;
    private javax.swing.JList<String> lstassigned;
    private javax.swing.JList<String> lstavailable;
    // End of variables declaration//GEN-END:variables

    private void clearLists() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
}
}
