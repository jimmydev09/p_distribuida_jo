package com.programacion.distribuida.rest;
import com.programacion.distribuida.clients.AuthorRestClient;
import com.programacion.distribuida.db.Book;
import com.programacion.distribuida.dtos.AuthorDto;
import com.programacion.distribuida.dtos.BookDto;
import com.programacion.distribuida.repo.BooksRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.stork.Stork;
import io.smallrye.stork.api.Service;
import io.smallrye.stork.api.ServiceInstance;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class BookRest {

    @Inject
    BooksRepository booksRepository;

    @Inject
    ModelMapper mapper;

    @Inject
    @RestClient
    AuthorRestClient client;


    // Metodo para buscar por ISBN con LISTA DE AUTORES.
    @GET
    @Path("/{isbn}")
    public Response findByIsbn(@PathParam("isbn") String isbn) {

    /*  var stork = Stork.getInstance();

        Map<String, Service> services = stork.getServices();
        services.entrySet().stream().forEach(it -> {
            var ser = it.getValue();
            ser.getInstances().subscribe().with(ls -> {
                ls.forEach(inst -> {
                   System.out.println("Instancia: " + inst.getId() + " - " + inst.getHost() + ":" + inst.getPort());
                });
            });
        });


        //Obtener una instancia (microservicio) del servicio "authors-api" registrada en Stork.
        //Esto devuelve una instancia disponible del servicio, por ejemplo, un microservicio de autores en el puerto 8080 o 8081.
        //Uni es una clase de Mutiny que representa un valor único que puede ser emitido en el futuro (una promesa).

        Service service = stork.getService("authors-api");
        Uni<ServiceInstance> instance = service.selectInstance();
        instance.subscribe().with(inst -> {
            System.out.println("Instancia seleccionada: " + inst.getId() + " - " + inst.getHost() + ":" + inst.getPort());
        });*/


        // 1. Buscar el Libro
        var obj = booksRepository.findByID(isbn);
        if (obj.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // 2. Crear el DTO para devolver el libro con los autores
        var book = obj.get();
        BookDto bookDto = generarBookDto(book);

        // 4. Devolver el DTO
        return Response.ok(bookDto).build();
    }

    @GET
    public List<BookDto> findAll() {
        return booksRepository.streamAll().map(book -> {
            BookDto bookDto = new BookDto();
            mapper.map(book, bookDto);
            var authors = client.findByBook(book.getIsbn()).stream().map(AuthorDto::getName).toList();
            bookDto.setAuthors(authors.stream().toList());
            return bookDto;
        }).toList();
    }

     /*
    /*
    Metodo para buscar todos los libros. Si el parámetro incluye=autores está presente, devuelve los libros con la lista de autores.
    Endpoint para obtener todos los libros: GET http://localhost:9090/books
    Endpoint para obtener todos los libros incluyendo autores: GET http://localhost:9090/books?incluye=autores
    /*
    @GET
    public Response findAll(@QueryParam("incluye") String incluye) {
        if ("autores".equals(incluye)) {
            // 1. Buscar todos los libros
            var listaLibros = booksRepository.listAll();
            if (listaLibros.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            List<BookDto> listaLibrosDto = new ArrayList<>();

            // 2. Crear el DTO para devolver los libros con los autores
            for (Book libro : listaLibros) {
                BookDto bookDto = generarBookDto(libro);

                // Agregar el libro DTO a la lista de libros DTO
                listaLibrosDto.add(bookDto);
            }


            // 4. Devolver el DTO
            return Response.ok(listaLibrosDto).build();

        } else {
            // 1. Buscar todos los libros
            var listaLibros = booksRepository.listAll();
            if (listaLibros.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // 2. Devolver la lista de libros
            return Response.ok(listaLibros).build();
        }

    }*/

    private BookDto generarBookDto(Book libro) {
        BookDto bookDto = new BookDto();
        bookDto.setIsbn(libro.getIsbn());
        bookDto.setTitle(libro.getTitle());
        bookDto.setPrice(libro.getPrice());

        // 2. Buscar el inventario y agregarlo al DTO
        var inventary = libro.getInventory();
        if (inventary != null) {
            bookDto.setInventorySold(inventary.getSold());
            bookDto.setInventorySupplied(inventary.getSupplied());
        }

        var miListadeAutores = client.findByBook(libro.getIsbn()).stream().map(AuthorDto::getName).toList();
        bookDto.setAuthors(miListadeAutores);

/*        // 3. Buscar los autores un servicio REST de otro microservicio
        var client = ClientBuilder.newClient();         // Crea una instancia de cliente HTTP usando ClientBuilder.newClient()

        // 3.1. Obtener la lista de autores. Llamar al servicio REST de autores y los devuelve como un array de AuthorDto
        AuthorDto[] listaAutores = client.target("") // URL del servicio REST de autores
                .path("/authors/find/{isbn}")
                .resolveTemplate("isbn", libro.getIsbn())
                .request(MediaType.APPLICATION_JSON)
                .get(AuthorDto[].class);

        // 3.2. Agregar la lista de Strings que seria solo los nombres de los autores al DTO.
        bookDto.setAuthors(Stream.of(listaAutores)
                .map(AuthorDto::getName)
                .toList()
        );*/
        return bookDto;
    }

}