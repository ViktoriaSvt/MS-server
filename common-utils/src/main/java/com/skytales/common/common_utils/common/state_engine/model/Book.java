package skytales.common.state_engine.model;

import jakarta.persistence.*;


import java.math.BigDecimal;
import java.util.UUID;


@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String author;

    @Column(name = "banner_image_url", length = 1000)
    private String bannerImageUrl;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private BigDecimal price;

    @Column( columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    public int quantity;

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getAuthor() {
        return author;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public Integer getYear() {
        return year;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Book(UUID id, String title, String genre, String author, String bannerImageUrl, String coverImageUrl, Integer year, BigDecimal price, String description, int quantity) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.author = author;
        this.bannerImageUrl = bannerImageUrl;
        this.coverImageUrl = coverImageUrl;
        this.year = year;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
    }

    public Book() {
    }
}
