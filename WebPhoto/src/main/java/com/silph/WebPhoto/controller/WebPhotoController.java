package com.silph.WebPhoto.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.silph.WebPhoto.model.Album;
import com.silph.WebPhoto.model.Photo;
import com.silph.WebPhoto.model.Photographer;
import com.silph.WebPhoto.repository.PhotographerRepository;
import com.silph.WebPhoto.service.AlbumService;
import com.silph.WebPhoto.service.PhotoService;
import com.silph.WebPhoto.service.PhotoValidator;
import com.silph.WebPhoto.service.PhotographerService;

@Controller
public class WebPhotoController {

	@Autowired
	private PhotographerService photographerService;
	@Autowired
	private PhotoService photoService;
	@Autowired
	private PhotoValidator photoValidator;
	@Autowired
	private AlbumService albumService;
	
	@RequestMapping("/") 
	public String home(Model model) {
		model.addAttribute("photos", this.photoService.getAllFoto());
		model.addAttribute("photographers", this.photographerService.getListaFotografi());
		return "index.html";
	}
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String adminPanel() {
		return "admin.html";
	}
	
	@RequestMapping("/photographers")
	public String getListaFotografi(Model model) {
		model.addAttribute("photographers", this.photographerService.getListaFotografi());
		return "fotografi.html";
	}
	
	@RequestMapping("/{username}")
	public String getFotografo(@PathVariable("username") String username, Model model) {
		Photographer photographer = this.photographerService.getByUsername(username);
		model.addAttribute("photographer", photographer);
		model.addAttribute("photos", this.photoService.getAllPhotoByAuthor(photographer));
		model.addAttribute("album", this.albumService.getByAuthor(photographer));
		return "photographer";
	}
	
	@RequestMapping("/newPhotographer")
	public String newStudente(Model model) {
		model.addAttribute("fotografo", new Photographer());
		return "fotografoForm.html";
	}
	
	@RequestMapping("/addFotografo")
	public String addFotografo(@ModelAttribute("fotografo") Photographer fotografo, Model model) {
		this.photographerService.inserisci(fotografo);
		return this.getListaFotografi(model);
	}
	
	@RequestMapping("/photo/{id}")
	public String getFoto(@PathVariable("id") Long id, Model model) {
		model.addAttribute("photo", this.photoService.getFoto(id));
		return "photo.html";
	}
	
	@RequestMapping("/{username}/album/{name}") 
	public String getAlbum(@PathVariable("username") String username,
							@PathVariable("name") String albumName, Model model) {
		Photographer author = photographerService.getByUsername(username);
		Album album = this.albumService.getByAuthorAndName(author, albumName);
		if (album != null) {
			model.addAttribute("album", album);
			model.addAttribute("photos", this.photoService.getPhotosByAlbum(album));
			return "album.html";
		} else {
			return "NotFound.html";
		}
	}
	
	@RequestMapping(value = "/uploadPhoto", method= RequestMethod.GET) 
	public String newFoto(Model model) {
		model.addAttribute("photo", new Photo());
		return "photoForm.html";
	}
	
	@RequestMapping(value = "/uploadPhoto", method = RequestMethod.POST)
	public String uploadPhoto(@Valid @ModelAttribute("photo") Photo photo,
								BindingResult bindingResult,
								@RequestParam("author") String username,
								@RequestParam("album") String albumName,
								Model model, WebRequest request) {
		
		this.photoValidator.validate(photo, bindingResult);{
	
			Photographer author = this.photographerService.getByUsername(username);
			if (author != null) {
				Album album = this.albumService.getByAuthorAndName(author, albumName);
				if (album != null) {
					Photo newPhoto = new Photo(photo.getName(), photo.getDescription(), author, album);
					this.photoService.upload(newPhoto);
					model.addAttribute("photos", this.photoService.getAllFoto());
					return "index";
				} else {
					model.addAttribute("msg", "Questo fotografo non ha nessun album con questo nome");
					return newFoto(model);
				}
			} else {
				model.addAttribute("msg", "Non esiste nessun fotografo con questo username");
				return newFoto(model);
			}
		}

	}
	
}
