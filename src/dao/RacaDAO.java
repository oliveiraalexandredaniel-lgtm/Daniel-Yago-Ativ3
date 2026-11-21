package dao;

import factory.ConnectionFactory;
import modelo.Raca;
import util.Validacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a dados e regras de negócio de Raça.
 *
 * Regras: nome e tipo (Cachorro ou Gato) obrigatórios; não permite o mesmo nome
 * duas vezes para o mesmo tipo (mas permite Poodle para Cachorro e para Gato);
 * ID nunca é alterado; exclusão lógica.
 */
public class RacaDAO {

    private static final String SELECT_BASE = "SELECT ID_Raca, Nome_Raca, Tipo_Animal, Status FROM raca";

    /** Cadastra uma raça (sempre ativa) e preenche o ID gerado no objeto. */
    public void inserir(Raca r) throws DAOException {
        validar(r);
        verificarDuplicidade(r.getNome(), r.getTipoAnimal(), 0);

        String sql = "INSERT INTO raca (Nome_Raca, Tipo_Animal, Status) VALUES (?, ?, TRUE)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNome());
            ps.setString(2, r.getTipoAnimal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }
            r.setStatus(true);
        } catch (SQLException e) {
            throw new DAOException("Erro ao cadastrar a raça: " + e.getMessage(), e);
        }
    }

    /** Altera nome e tipo de uma raça ativa. O ID e o status não são alterados. */
    public void alterar(Raca r) throws DAOException {
        Raca atual = buscarPorId(r.getId());
        if (atual == null) {
            throw new RegraNegocioException("Raça não encontrada (ID " + r.getId() + ").");
        }
        if (!atual.isStatus()) {
            throw new RegraNegocioException("Raça inativa não pode ser alterada. Reative-a primeiro.");
        }
        validar(r);
        if (!atual.getTipoAnimal().equals(r.getTipoAnimal()) && contarAnimais(r.getId(), false) > 0) {
            throw new RegraNegocioException("Não é possível mudar o tipo de uma raça que já possui animais vinculados.");
        }
        verificarDuplicidade(r.getNome(), r.getTipoAnimal(), r.getId());

        String sql = "UPDATE raca SET Nome_Raca = ?, Tipo_Animal = ? WHERE ID_Raca = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getNome());
            ps.setString(2, r.getTipoAnimal());
            ps.setInt(3, r.getId());
            ps.executeUpdate();
            r.setStatus(true);
        } catch (SQLException e) {
            throw new DAOException("Erro ao alterar a raça: " + e.getMessage(), e);
        }
    }

    /** Exclusão lógica. Não permite desativar raça que ainda tenha animais ativos. */
    public void desativar(int id) throws DAOException {
        Raca atual = buscarPorId(id);
        if (atual == null) {