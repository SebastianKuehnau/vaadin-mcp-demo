package org.vaadin.demo.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.vaadin.demo.data.Article;
import org.vaadin.demo.data.ArticleRepository;

@Service
public class ArticleService implements CommandLineRunner {

    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public Optional<Article> get(Long id) {
        return repository.findById(id);
    }

    public Article save(Article entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Article> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Article> list(Pageable pageable, Specification<Article> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    @Override
    public void run(String... args) throws Exception {
        // Create example data only if the database is empty
        if (repository.count() == 0) {
            createExampleData();
        }
    }

    private void createExampleData() {
        Article article1 = new Article();
        article1.setName("Laptop");
        article1.setPrice(new BigDecimal("899.99"));
        article1.setDescription("High-performance laptop with 16GB RAM and 512GB SSD");
        article1.setProductionDate(LocalDate.of(2024, 1, 15));
        repository.save(article1);

        Article article2 = new Article();
        article2.setName("Wireless Mouse");
        article2.setPrice(new BigDecimal("29.99"));
        article2.setDescription("Ergonomic wireless mouse with long battery life");
        article2.setProductionDate(LocalDate.of(2024, 2, 10));
        repository.save(article2);

        Article article3 = new Article();
        article3.setName("Mechanical Keyboard");
        article3.setPrice(new BigDecimal("149.99"));
        article3.setDescription("RGB mechanical keyboard with Cherry MX switches");
        article3.setProductionDate(LocalDate.of(2024, 3, 5));
        repository.save(article3);

        Article article4 = new Article();
        article4.setName("4K Monitor");
        article4.setPrice(new BigDecimal("399.99"));
        article4.setDescription("27-inch 4K UHD monitor with HDR support");
        article4.setProductionDate(LocalDate.of(2024, 1, 20));
        repository.save(article4);

        Article article5 = new Article();
        article5.setName("USB-C Hub");
        article5.setPrice(new BigDecimal("59.99"));
        article5.setDescription("7-in-1 USB-C hub with HDMI, USB 3.0, and SD card reader");
        article5.setProductionDate(LocalDate.of(2024, 4, 12));
        repository.save(article5);

        Article article6 = new Article();
        article6.setName("Office Chair");
        article6.setPrice(new BigDecimal("279.99"));
        article6.setDescription("Ergonomic office chair with lumbar support and adjustable armrests");
        article6.setProductionDate(LocalDate.of(2024, 2, 28));
        repository.save(article6);
    }
}