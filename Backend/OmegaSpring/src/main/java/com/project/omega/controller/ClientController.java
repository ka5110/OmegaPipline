package com.project.omega.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.omega.bean.dao.entity.Client;
import com.project.omega.exceptions.ClientNotFoundException;
import com.project.omega.exceptions.NoRecordsFoundException;
import com.project.omega.repository.ClientRepository;
import com.project.omega.service.interfaces.ClientService;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.lang.model.element.VariableElement;
//import javax.xml.ws.Response;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/client")
public class ClientController {


    @Autowired
    ClientService clientService;


    private ObjectMapper mapper = new ObjectMapper();


    @PostMapping (value= "/create",  headers = "Accept=application/json")
    public ResponseEntity<Client> createClient(@RequestBody Client client) throws Exception {
        Client newClient  = clientService.createClient(client);

        Mail mail = new Mail();
        Email fromEmail = new Email();
        mail.setFrom(fromEmail);

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setName("emailPojo.getToName()");
        to.setEmail("khalil.alhabal@gmail.com");
        personalization.addTo(to);

//
//        String email = "khalil.alhabal@gmail.com";
//        sendEmail(email);


        return new ResponseEntity(newClient, HttpStatus.CREATED);

    }
    @GetMapping (value = "/get")
    public ResponseEntity<Client> GetAllClients() throws NoRecordsFoundException {
        List<Client> clients = clientService.getAllClients();

        return new ResponseEntity(clients, HttpStatus.OK);

    }

    @GetMapping (value = "/get/{id}")
    public ResponseEntity getClient (@PathVariable (value="id") Long id) throws ClientNotFoundException, NoRecordsFoundException {
        Client client = clientService.getClientById(id);

        return new ResponseEntity(client, HttpStatus.OK);

    }

//    @PostMapping (value = "/get/")
//    public ResponseEntity  getClientName (@RequestBody String name) throws NoRecordsFoundException {
//        List <Client> clients = clientService.getClientsbySearchQuery(name);
//        return new ResponseEntity (clients, HttpStatus.OK);
//
//    }

    @DeleteMapping (value = "/delete/{id}")
    public ResponseEntity deleteClient (@PathVariable (value = "id") Long id) throws ClientNotFoundException {
        Optional<Client> client = clientService.deleteClientById(id);
        return new ResponseEntity(client, HttpStatus.OK);


    }

    @PutMapping (value = "/update/{id}")
    public ResponseEntity updateClient (@PathVariable (value= "id") Long id, @RequestBody Client updated) throws ClientNotFoundException {
        Client client = clientService.updateClientById(id, updated);
        return new ResponseEntity(client, HttpStatus.OK);

    }


}


