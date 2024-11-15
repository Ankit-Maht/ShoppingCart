package com.ecomm.controller;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecomm.model.Category;
import com.ecomm.model.Product;
import com.ecomm.service.CategoryService;
import com.ecomm.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}
	
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
    List<Category> categories = categoryService.getAllCategory();
    m.addAttribute("categories", categories);
		return "admin/add_product";
	}
	
	@GetMapping("/category")
	public String category(Model m) {
		m.addAttribute("categories",categoryService.getAllCategory());
		return "admin/category";
	}
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file ,HttpSession session) throws IOException {
		String imageName=  file!=null ? file.getOriginalFilename():"defualt.jpg";
		category.setImageName(imageName);
		
		Boolean existCategory = categoryService.existCategory(category.getName());
		
		if(existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		}
		else {
			Category saveCategory=categoryService.saveCategory(category);
			if(ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Not Saved! internal server error");
			}
			else {
				

					File saveFile = new ClassPathResource("static/images").getFile();
					Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+"category"+File.separator+file.getOriginalFilename());
					System.out.println(path);
					Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				
				session.setAttribute("succMsg", "Saved Successfully");
			}
		}
		
		return "redirect:/admin/category";
	}

	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id,HttpSession session) {
		Boolean deleteCategory=categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("succMsg", "Category deleted Successfully");
		}
		else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/category";
	}
	
	
	
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id,Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}
	
	
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file,HttpSession session) throws IOException {
		
	    Category oldCategory=categoryService.getCategoryById(category.getId());
	    String imageName= file.isEmpty()? oldCategory.getImageName():file.getOriginalFilename();
	    
	    if(!ObjectUtils.isEmpty(oldCategory)) {
	    	
	    	if(!file.isEmpty()) {
	    		File saveFile = new ClassPathResource("static/images").getFile();
				Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+"category"+File.separator+file.getOriginalFilename());
				System.out.println(path);
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
	    	}
	    		
	    	
	    	oldCategory.setName(category.getName());
	    	oldCategory.setIsActive(category.getIsActive());
	    	oldCategory.setImageName(imageName);
	    	
	    }
	    Category updateCategory =categoryService.saveCategory(oldCategory);
	    
	    if(!ObjectUtils.isEmpty(updateCategory)) {
	    	session.setAttribute("succMsg", "Category Updated Successfully");
	    }
	    else {
	    	session.setAttribute("errorMsg", "Something wrong on server");
	    }
		
		return "redirect:/admin/loadEditCategory/" +category.getId();
	}
	
	
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image,HttpSession session) throws IOException {
		
	    String imageName=image.isEmpty()?"default.jpg": image.getOriginalFilename();
	    product.setImage(imageName);
		
	    Product saveProduct=productService.saveProduct(product);
	    
	    if(!ObjectUtils.isEmpty(saveProduct)) {
	    	
	    	File saveFile = new ClassPathResource("static/images").getFile();
			Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+"product"+File.separator+image.getOriginalFilename());
			System.out.println(path);
			Files.copy(image.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
	    	
	    	session.setAttribute("succMsg", "Product saved Successfully");
	    }
	    else {
	    	session.setAttribute("errorMsg", "Something went wrong on server");
	    }
	    
		return "redirect:/admin/loadAddProduct";
	}
	
	@GetMapping("/products")
	public String loadViewProduct(Model m) {
		m.addAttribute("products",productService.getAllProduct());
		return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id,HttpSession session) {
	Boolean deleteProductBoolean=productService.deleteProduct(id);
	if(deleteProductBoolean) {
		session.setAttribute("succMsg", "Product Deleted Successfully");
	}
	else {
		session.setAttribute("errorMsg", "Something went wrong! Product not deleted");
	}
	return "redirect:/admin/products";
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id,Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}
	
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image,HttpSession session,Model m) {
	Product  updateProduct = productService.updateProduct(product, image);
	
	if(!ObjectUtils.isEmpty(updateProduct)) {
		session.setAttribute("succMsg", "Product updated Successfully");
	}
	else {
		session.setAttribute("errorMsg", "Something went wrong! Product not updated");
	}
	
	return "redirect:/admin/editProduct/" + product.getId();
	}
	
}

