package org.vaadin.demo.views.articlemanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.demo.data.Article;
import org.vaadin.demo.services.ArticleService;
import org.vaadin.demo.views.MainLayout;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Article Management")
@Route(value = "article-management/:articleID?/:action?(edit)", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class ArticleManagementView extends Div implements BeforeEnterObserver {

    private final String ARTICLE_ID = "articleID";
    private final String ARTICLE_EDIT_ROUTE_TEMPLATE = "article-management/%s/edit";

    private final Grid<Article> grid = new Grid<>(Article.class, false);

    private TextField name;
    private BigDecimalField price;
    private TextArea description;
    private DatePicker productionDate;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");

    private final BeanValidationBinder<Article> binder;

    private Article article;

    private final ArticleService articleService;

    public ArticleManagementView(ArticleService articleService) {
        this.articleService = articleService;
        addClassNames("article-management-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setHeader("Name").setAutoWidth(true);

        NumberFormat euroFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        grid.addColumn(new NumberRenderer<>(Article::getPrice, euroFormat))
            .setHeader("Price")
            .setAutoWidth(true)
            .setComparator((a1, a2) -> a1.getPrice().compareTo(a2.getPrice()));

        grid.addColumn("description").setHeader("Description").setAutoWidth(true);
        grid.addColumn("productionDate").setHeader("Production Date").setAutoWidth(true);

        grid.setItems(query -> articleService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ARTICLE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ArticleManagementView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Article.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.article == null) {
                    this.article = new Article();
                }
                binder.writeBean(this.article);
                articleService.save(this.article);
                clearForm();
                refreshGrid();
                Notification.show("Article saved successfully");
                UI.getCurrent().navigate(ArticleManagementView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to save the article. Check again that all values are valid");
            }
        });

        delete.addClickListener(e -> {
            if (this.article != null) {
                articleService.delete(this.article.getId());
                clearForm();
                refreshGrid();
                Notification.show("Article deleted successfully");
                UI.getCurrent().navigate(ArticleManagementView.class);
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> articleId = event.getRouteParameters().get(ARTICLE_ID).map(Long::parseLong);
        if (articleId.isPresent()) {
            Optional<Article> articleFromBackend = articleService.get(articleId.get());
            if (articleFromBackend.isPresent()) {
                populateForm(articleFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested article was not found, ID = %s", articleId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ArticleManagementView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        price = new BigDecimalField("Price (EUR)");
        price.setPrefixComponent(new Div("€"));
        description = new TextArea("Description");
        description.setHeight("150px");
        productionDate = new DatePicker("Production Date");
        formLayout.add(name, price, description, productionDate);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, delete, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Article value) {
        this.article = value;
        binder.readBean(this.article);
    }
}
