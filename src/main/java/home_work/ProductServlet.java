package home_work;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@WebServlet(name = "ProductServlet", urlPatterns = "/product_servlet")
public class ProductServlet implements Servlet {

    private transient ServletConfig config;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            products.add(new Product(i, "title product " + i, Math.random() * 1000));
        }

        for (int i = 0; i < 10; i++) {
            servletResponse.getWriter().format("Product id = %d Title = %s Cost = %3.2f %n", products.get(i).getId(),
                    products.get(i).getTitle(), products.get(i).getCost());
        }

        //servletResponse.getWriter().println(products);
    }

    @Override
    public String getServletInfo() {
        return "ProductServlet";
    }

    @Override
    public void destroy() {

    }
}
