package jenry.springboot.repository;

import jenry.springboot.model.Pessoa;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    @Query("select p from Pessoa p where p.nome like %?1%")
    List<Pessoa> findPessoaByName(String nome);

    @Query("select p from Pessoa p where p.nome like %?1% and p.sexo = ?2" )
    List<Pessoa> findPessoaByNameAndSexo(String nome, String sexo);
    
    @Query("select p from Pessoa p where p.sexo = ?1")
    List<Pessoa> findPessoaBySexo(String sexo);

    default Page<Pessoa> findPessoaByNamePage(String nome, Pageable pageable){
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(nome);

        //Configurando pesquisa para consultar por partes do nome do banco de dados, igual ao like do sql
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher(
                "nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        //une o objeto com o valor e a configuração para consultar
        Example<Pessoa> example = Example.of(pessoa,exampleMatcher);

        Page<Pessoa> pessoas = findAll(example, pageable);

        return pessoas;
    }

    default Page<Pessoa> findPessoaByNameAndSexoPage(String nome,String sexo, Pageable pageable){
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(nome);
        pessoa.setSexo(sexo);

        //Configurando pesquisa para consultar por partes do nome do banco de dados, igual ao like do sql
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher(
                "nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("sexo", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());


                //.withMatcher("sexo", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        //une o objeto com o valor e a configuração para consultar
        Example<Pessoa> example = Example.of(pessoa,exampleMatcher);

        Page<Pessoa> pessoas = findAll(example, pageable);

        return pessoas;
    }
}
