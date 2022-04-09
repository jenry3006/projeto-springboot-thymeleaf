package jenry.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import jenry.springboot.repository.ProfissaoRepository;
import net.sf.jasperreports.phantomjs.InetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jenry.springboot.model.Pessoa;
import jenry.springboot.model.Telefone;
import jenry.springboot.repository.PessoaRepository;
import jenry.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private TelefoneRepository telefoneRepository;
    
    @Autowired
    private ReportUtil reportUtil;

    @Autowired
    private ProfissaoRepository profissaoRepository;

    //@RequestMapping(method = RequestMethod.GET,value = "/cadastroPessoa")
    @GetMapping("**/cadastroPessoa")
    public ModelAndView inicio(){
        ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
        modelAndView.addObject("pessoaObj", new Pessoa());
        modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0,5, Sort.by("nome"))));
        modelAndView.addObject("profissoes", profissaoRepository.findAll());
        return modelAndView;
    }

    //o ** ignora tudo que vem antes do "/salvarPessoa"
    //@RequestMapping(method = RequestMethod.POST, value = "**/salvarPessoa")
    @PostMapping(value = "**/salvarPessoa", consumes = {"multipart/form-data"})
    public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {

        System.out.println(file.getContentType());
        System.out.println(file.getName());
        System.out.println(file.getOriginalFilename());

        //passando os telefones junto para poder salvar quando editar uma pessoa com telefones cadastados
        pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
        if(bindingResult.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
            modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0,5, Sort.by("nome"))));
            modelAndView.addObject("pessoaObj", pessoa);

            List<String> msgError = new ArrayList<String>();
            for (ObjectError objectError : bindingResult.getAllErrors()){
                msgError.add(objectError.getDefaultMessage()); //Vem das anotações de validações
            }

            modelAndView.addObject("msgError", msgError);
            modelAndView.addObject("profissoes", profissaoRepository.findAll());

            return modelAndView;
        }

        if(file.getSize() > 0){
            pessoa.setFoto(file.getBytes());
            pessoa.setTipoFileFoto(file.getContentType());
            pessoa.setNomeFileFoto(file.getOriginalFilename());
        } else{
            if(pessoa.getId() != null && pessoa.getId() > 0){
                Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
                pessoa.setFoto(pessoaTemp.getFoto());
                pessoa.setTipoFileFoto(pessoaTemp.getTipoFileFoto());
                pessoa.setNomeFileFoto(pessoaTemp.getNomeFileFoto());
            }
        }

        pessoaRepository.save(pessoa);
        ModelAndView andView = new ModelAndView("cadastro/cadastroPessoa");
        andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0,5, Sort.by("nome"))));
        andView.addObject("pessoaObj", new Pessoa());
        return andView;
    }

    @GetMapping("**/listaPessoas")
    public ModelAndView pessoas(){
        ModelAndView andView = new ModelAndView("cadastro/cadastroPessoa");
        andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0,5, Sort.by("nome"))));
        andView.addObject("pessoaObj", new Pessoa());

        return  andView;
    }

    @GetMapping("/pessoasPag")
    public ModelAndView carregaPessoaPaginacao(@PageableDefault(size = 5) Pageable pageable,
                                               ModelAndView modelAndView, @RequestParam("nomePesquisa") String nomePesquisa){
        Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomePesquisa, pageable);
        modelAndView.addObject("pessoas",pagePessoa);
        modelAndView.addObject("pessoaObj",new Pessoa());
        modelAndView.addObject("nomePesquisa", nomePesquisa);
        modelAndView.setViewName("cadastro/cadastroPessoa");

        return modelAndView;
    }

    @GetMapping("/editarPessoa/{idPessoa}")
    public ModelAndView editar(@PathVariable("idPessoa") Long idPessoa){
        ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
        Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
        modelAndView.addObject("pessoaObj", pessoa.get());
        modelAndView.addObject("profissoes", profissaoRepository.findAll());

        return modelAndView;
    }

    @GetMapping("/excluirPessoa/{idPessoa}")
    public ModelAndView excluir(@PathVariable("idPessoa") Long idPessoa){
        pessoaRepository.deleteById(idPessoa);
        ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
        modelAndView.addObject("pessoas",pessoaRepository.findAll(PageRequest.of(0,5, Sort.by("nome"))));
        modelAndView.addObject("pessoaObj", new Pessoa());
        return modelAndView;
    }

    @PostMapping("**/pesquisarPessoa")
    public ModelAndView pesquisar(
        @RequestParam("nomePesquisa") String nomePesquisa,
        @RequestParam("sexoPesquisa") String sexoPesquisa, @PageableDefault(size = 5, sort = {"nome"}) Pageable pageable){ //@RequestParam é oque o usuario vai digitar no input do form.

       Page<Pessoa> pessoas = null;

        if (sexoPesquisa != null && !sexoPesquisa.isEmpty()){
            pessoas = pessoaRepository.findPessoaByNameAndSexoPage(nomePesquisa,sexoPesquisa, pageable);

        } else {
            pessoas = pessoaRepository.findPessoaByNamePage(nomePesquisa, pageable);
        }

        ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
        modelAndView.addObject("pessoas", pessoas);
        modelAndView.addObject("pessoaObj", new Pessoa());
        modelAndView.addObject("nomePesquisa", nomePesquisa);
        return modelAndView;
    }

    @GetMapping("**/baixarCurriculo/{idPessoa}")
    public void baixarCurriculo(@PathVariable ("idPessoa") Long idPessoa, HttpServletResponse response) throws IOException {

        Pessoa pessoa = pessoaRepository.findById(idPessoa).get();

        if (pessoa.getFoto() != null){
            //setar tamanho da  resposta
            response.setContentLength(pessoa.getFoto().length);
            //tipo arquivo para download
            response.setContentType(pessoa.getTipoFileFoto());
            //
            String headerKey = "Conten-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"",pessoa.getNomeFileFoto());
            response.setHeader(headerKey,headerValue);

            response.getOutputStream().write(pessoa.getFoto());
        }
    }
    
    @GetMapping("**/pesquisarPessoa")
    public void imprimirPdf(
        @RequestParam("nomePesquisa") String nomePesquisa,@RequestParam("sexoPesquisa") String sexoPesquisa
        ,HttpServletRequest request, HttpServletResponse response) throws Exception{

    	List<Pessoa> pessoas = new ArrayList<Pessoa>();
    	
    	if(sexoPesquisa != null && !sexoPesquisa.isEmpty() 
    			&& nomePesquisa != null && !nomePesquisa.isEmpty()) {
    		
    		pessoas = pessoaRepository.findPessoaByNameAndSexo(nomePesquisa, sexoPesquisa);
    		
    	} else if(nomePesquisa != null && nomePesquisa.isEmpty()){
    		
			pessoas = pessoaRepository.findPessoaByName(nomePesquisa);
			
		} else if(sexoPesquisa != null && sexoPesquisa.isEmpty()){
			
			pessoas = pessoaRepository.findPessoaBySexo(sexoPesquisa);
			
		}
    	/*else {
    		
			Iterable<Pessoa> iterable = pessoaRepository.findAll();
			
			for (Pessoa pessoa : iterable) {
				pessoas.add(pessoa);
			}
		}*/
    	
    	//chama o serviço que faz a geração de relatorio
    	byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoas", request.getServletContext()); 
    	
    	//tamanho da resposta
    	response.setContentLength(pdf.length);

    	//dafeinir na resposta do arquivo
    	response.setContentType("application/octet-stream");
    	
    	//definir o cabealho da resposta
    	String headerKey = "Content-Disposition";
    	String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
    	response.setHeader(headerKey, headerValue);
    	
    	//finaliza a resposta para o navegador
    	response.getOutputStream().write(pdf);
    }

    @GetMapping("/contatos/{idPessoa}")
    public ModelAndView contato(@PathVariable("idPessoa") Long idPessoa){
        ModelAndView modelAndView = new ModelAndView("cadastro/contatos");
        Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
        modelAndView.addObject("pessoaObj", pessoa.get());
        modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));

        return modelAndView;
    }

    @PostMapping("**/addFonePessoa/{idPessoa}")
    public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("idPessoa") Long idPessoa){
        Pessoa pessoa = pessoaRepository.findById(idPessoa).get();

        if(telefone != null && (telefone.getNumero() != null && telefone.getNumero().isEmpty()
                || telefone.getTipo().isEmpty())){
            ModelAndView modelAndView = new ModelAndView("cadastro/contatos");
            modelAndView.addObject("pessoaObj", pessoa);
            modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));
            List<String> msg = new ArrayList<String>();
            if (telefone.getNumero().isEmpty()){
                msg.add("Número deve ser informado.");
            }
            if (telefone.getTipo().isEmpty()){
                msg.add("Tipo deve ser informado.");
            }
            modelAndView.addObject("msg", msg);
            return modelAndView;
        }
        telefone.setPessoa(pessoa);
        telefoneRepository.save(telefone);
        ModelAndView modelAndView = new ModelAndView("cadastro/contatos");
        modelAndView.addObject("pessoaObj", pessoa);
        modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));
        return modelAndView;
    }

    @GetMapping("/excluirTelefone/{idTelefone}")
    public ModelAndView excluirTelefone(@PathVariable("idTelefone") Long idTelefone){

        Pessoa pessoa = telefoneRepository.findById(idTelefone).get().getPessoa();
        telefoneRepository.deleteById(idTelefone);
        ModelAndView modelAndView = new ModelAndView("cadastro/contatos");
        modelAndView.addObject("pessoaObj", pessoa);
        modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
        return modelAndView;
    }

}
