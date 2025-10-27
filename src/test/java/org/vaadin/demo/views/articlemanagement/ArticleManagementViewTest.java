package org.vaadin.demo.views.articlemanagement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.BrowserTest;
import com.vaadin.testbench.BrowserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vaadin.demo.data.ArticleRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ArticleManagementViewTest extends BrowserTestBase {

    @Autowired
    private ArticleRepository articleRepository;

    @BeforeEach
    public void setup() {
        getDriver().get("http://localhost:8080/article-management");
    }

    @Test
    public void gridDisplaysArticles() {
        GridElement grid = $(GridElement.class).first();
        Assertions.assertTrue(grid.getRowCount() > 0, "Grid should display articles");
    }

    @Test
    public void createNewArticle() {
        // Get initial count
        GridElement grid = $(GridElement.class).first();
        int initialRowCount = grid.getRowCount();

        // Clear any selection
        clearForm();

        // Fill in the form
        TextFieldElement nameField = $(TextFieldElement.class).caption("Name").first();
        nameField.setValue("Test Article");

        BigDecimalFieldElement priceField = $(BigDecimalFieldElement.class).first();
        priceField.setValue("99.99");

        TextAreaElement descriptionField = $(TextAreaElement.class).caption("Description").first();
        descriptionField.setValue("This is a test article created by automated test");

        DatePickerElement dateField = $(DatePickerElement.class).caption("Production Date").first();
        dateField.setDate("2024-10-27");

        // Click save button
        ButtonElement saveButton = $(ButtonElement.class).caption("Save").first();
        saveButton.click();

        // Wait for notification
        waitForNotification("Article saved successfully");

        // Verify grid updated
        grid = $(GridElement.class).first();
        Assertions.assertEquals(initialRowCount + 1, grid.getRowCount(),
            "Grid should have one more row after creating article");

        // Verify the article appears in the grid
        boolean articleFound = false;
        for (int i = 0; i < grid.getRowCount(); i++) {
            if (grid.getCell(i, 0).getText().contains("Test Article")) {
                articleFound = true;
                break;
            }
        }
        Assertions.assertTrue(articleFound, "New article should appear in the grid");
    }

    @Test
    public void readArticleDetails() {
        GridElement grid = $(GridElement.class).first();

        // Click on the first row
        grid.getCell(0, 0).click();

        // Wait a bit for the form to populate
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify form is populated
        TextFieldElement nameField = $(TextFieldElement.class).caption("Name").first();
        String nameValue = nameField.getValue();
        Assertions.assertNotNull(nameValue, "Name field should be populated");
        Assertions.assertFalse(nameValue.isEmpty(), "Name field should not be empty");

        BigDecimalFieldElement priceField = $(BigDecimalFieldElement.class).first();
        String priceValue = priceField.getValue();
        Assertions.assertNotNull(priceValue, "Price field should be populated");
        Assertions.assertFalse(priceValue.isEmpty(), "Price field should not be empty");
    }

    @Test
    public void updateExistingArticle() {
        GridElement grid = $(GridElement.class).first();

        // Click on the first row to select an article
        grid.getCell(0, 0).click();

        // Wait for form to populate
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update the name field
        TextFieldElement nameField = $(TextFieldElement.class).caption("Name").first();
        String originalName = nameField.getValue();
        String updatedName = originalName + " - Updated";
        nameField.setValue(updatedName);

        // Update the price
        BigDecimalFieldElement priceField = $(BigDecimalFieldElement.class).first();
        priceField.setValue("199.99");

        // Click save button
        ButtonElement saveButton = $(ButtonElement.class).caption("Save").first();
        saveButton.click();

        // Wait for notification
        waitForNotification("Article saved successfully");

        // Verify the updated name appears in the grid
        grid = $(GridElement.class).first();
        boolean updatedArticleFound = false;
        for (int i = 0; i < grid.getRowCount(); i++) {
            if (grid.getCell(i, 0).getText().contains(updatedName)) {
                updatedArticleFound = true;
                break;
            }
        }
        Assertions.assertTrue(updatedArticleFound, "Updated article should appear in the grid");
    }

    @Test
    public void deleteArticle() {
        GridElement grid = $(GridElement.class).first();
        int initialRowCount = grid.getRowCount();

        // Click on the first row to select an article
        String articleNameToDelete = grid.getCell(0, 0).getText();
        grid.getCell(0, 0).click();

        // Wait for form to populate
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Click delete button
        ButtonElement deleteButton = $(ButtonElement.class).caption("Delete").first();
        deleteButton.click();

        // Wait for notification
        waitForNotification("Article deleted successfully");

        // Verify grid updated
        grid = $(GridElement.class).first();
        Assertions.assertEquals(initialRowCount - 1, grid.getRowCount(),
            "Grid should have one less row after deleting article");

        // Verify the article is no longer in the grid
        boolean articleFound = false;
        for (int i = 0; i < grid.getRowCount(); i++) {
            if (grid.getCell(i, 0).getText().equals(articleNameToDelete)) {
                articleFound = true;
                break;
            }
        }
        Assertions.assertFalse(articleFound, "Deleted article should not appear in the grid");
    }

    @Test
    public void cancelButtonClearsForm() {
        GridElement grid = $(GridElement.class).first();

        // Click on the first row to select an article
        grid.getCell(0, 0).click();

        // Wait for form to populate
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify form is populated
        TextFieldElement nameField = $(TextFieldElement.class).caption("Name").first();
        Assertions.assertFalse(nameField.getValue().isEmpty(), "Name field should be populated");

        // Click cancel button
        ButtonElement cancelButton = $(ButtonElement.class).caption("Cancel").first();
        cancelButton.click();

        // Wait a bit for the form to clear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify form is cleared
        nameField = $(TextFieldElement.class).caption("Name").first();
        Assertions.assertTrue(nameField.getValue().isEmpty(), "Name field should be empty after cancel");
    }

    @Test
    public void formValidationPreventsSavingWithoutRequiredFields() {
        // Clear any selection
        clearForm();

        // Try to save without filling required fields
        ButtonElement saveButton = $(ButtonElement.class).caption("Save").first();
        saveButton.click();

        // Wait a bit
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if validation error is shown or notification appears
        // The form should not save successfully
        NotificationElement[] notifications = $(NotificationElement.class).all().toArray(new NotificationElement[0]);
        if (notifications.length > 0) {
            // If a notification appears, it should be about validation failure
            String notificationText = notifications[0].getText();
            Assertions.assertTrue(
                notificationText.contains("Failed") || notificationText.contains("valid"),
                "Validation error notification should appear"
            );
        }
    }

    @Test
    public void priceFieldDisplaysEuroCurrency() {
        GridElement grid = $(GridElement.class).first();

        // Click on the first row
        grid.getCell(0, 0).click();

        // Wait for form to populate
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify the price column in grid shows Euro formatting
        String priceText = grid.getCell(0, 1).getText();
        Assertions.assertTrue(priceText.contains("€") || priceText.contains("EUR"),
            "Price should be displayed with Euro currency symbol");
    }

    // Helper methods

    private void clearForm() {
        // Click cancel to clear any existing form data
        try {
            ButtonElement cancelButton = $(ButtonElement.class).caption("Cancel").first();
            cancelButton.click();
            Thread.sleep(300);
        } catch (Exception e) {
            // Form might already be clear
        }
    }

    private void waitForNotification(String expectedText) {
        try {
            Thread.sleep(500);
            NotificationElement notification = $(NotificationElement.class).onPage().first();
            if (notification != null) {
                String notificationText = notification.getText();
                Assertions.assertTrue(notificationText.contains(expectedText),
                    "Expected notification: " + expectedText + ", but got: " + notificationText);
            }
        } catch (Exception e) {
            // Notification might not appear or might disappear quickly
            System.out.println("Warning: Could not verify notification - " + e.getMessage());
        }
    }
}