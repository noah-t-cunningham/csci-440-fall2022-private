package edu.montana.csci.csci440.model;

import java.math.BigDecimal;

public class InvoiceItem extends Model {

    Long invoiceLineId;
    Long invoiceId;
    Long trackId;
    BigDecimal unitPrice;
    Long quantity;
    // getInvoieItems vars
    String trackName;
    String albumName;
    String artistName;


    public Track getTrack() {
        return null;
    }
    public Invoice getInvoice() {
        return null;
    }

    public Long getInvoiceLineId() {
        return invoiceLineId;
    }

    public void setInvoiceLineId(Long invoiceLineId) {
        this.invoiceLineId = invoiceLineId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    // getInvoiceItems method variable getters/setters
    public String getTrackName() {
        return trackName;
    }
    public void setTrackName(String name) {
        this.trackName = name;
    }
    public String getAlbumName() {
        return albumName;
    }
    public void setAlbumName(String name) {
        this.albumName = name;
    }
    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String name) {
        this.artistName = name;
    }
}
