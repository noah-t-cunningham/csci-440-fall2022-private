package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Invoice extends Model {

    Long invoiceId;
    String billingAddress;
    String billingCity;
    String billingState;
    String billingCountry;
    String billingPostalCode;
    BigDecimal total;

    public Invoice() {
        // new employee for insert
    }

    Invoice(ResultSet results) throws SQLException {
        billingAddress = results.getString("BillingAddress");
        billingState = results.getString("BillingState");
        billingCountry = results.getString("BillingCountry");
        billingPostalCode = results.getString("BillingPostalCode");
        total = results.getBigDecimal("Total");
        invoiceId = results.getLong("InvoiceId");
        billingCity = results.getString("BillingCity");

    }

    public List<InvoiceItem> getInvoiceItems(){
        //TODO implement
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT *, t.Name as trackName, a.Title as albumName, a2.Name as artistName FROM invoice_items\n" +
                             "JOIN tracks t on t.TrackId = invoice_items.TrackId\n" +
                             "JOIN albums a on a.AlbumId = t.AlbumId\n" +
                             "JOIN artists a2 on a2.ArtistId = a.ArtistId\n" +
                             "WHERE invoice_items.InvoiceId = ?"
             )) {
            stmt.setLong(1, invoiceId);
            ResultSet results = stmt.executeQuery();
            List<InvoiceItem> resultList = new LinkedList<>();
            while (results.next()) {
                InvoiceItem ii = new InvoiceItem();
                ii.setInvoiceLineId(results.getLong("InvoiceLineId"));
                ii.setInvoiceId(results.getLong("InvoiceId"));
                ii.setTrackId(results.getLong("TrackId"));
                ii.setUnitPrice(results.getBigDecimal("UnitPrice"));
                ii.setQuantity(results.getLong("Quantity"));
                ii.setTrackName(results.getString("TrackName"));
                ii.setAlbumName(results.getString("AlbumName"));
                ii.setArtistName(results.getString("ArtistName"));
                resultList.add(ii);
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        //return Collections.emptyList();
    }
    public Customer getCustomer() {
        return null;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public static List<Invoice> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Invoice> all(int page, int count) {
        int offset = offset(page, count);
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM invoices LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, offset);

            ResultSet results = stmt.executeQuery();
            List<Invoice> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Invoice(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Invoice find(long invoiceId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM invoices WHERE InvoiceId=?")) {
            stmt.setLong(1, invoiceId);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Invoice(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
}
