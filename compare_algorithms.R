suppressPackageStartupMessages({
  library(dplyr)
  library(magrittr)
  library(ggplot2)
  library(reshape2)
})
source('R/functions.R')

algo_data = read.csv('./results-existing-all/results-existing-cost-4/ResultsGeneral.csv', stringsAsFactors = F) %>%
  dplyr::mutate(
    blind_makespan_winner = blind_makespan_winner %>% tidy_logical(),
    blind_cost_winner = blind_cost_winner %>% tidy_logical(),
    blind_absolute_cost_winner = blind_absolute_cost_winner %>% tidy_logical(),
    blind_absolute_makespan_winner = blind_absolute_makespan_winner %>% tidy_logical()) %>%
  dplyr::rename(temp_id = id) %>%
  dplyr::mutate(id = stringr::str_extract(string = temp_id, pattern = "\\d+") %>% as.numeric())

algo_mk = reshape2::melt(algo_data, 
                         id.vars = "id", 
                         measure.vars = c("mk_blind", "mk_maxmin", "mk_minmin", "mk_myopic"),
                         variable.name = "mk") %>%
  dplyr::mutate(algo_name = sapply(stringr::str_split(mk, "_"), function(x) x[[2]])) %>%
  dplyr::select(id, algo_name, value) %>%
  dplyr::rename(mk = value)

algo_cost = reshape2::melt(algo_data, 
                           id.vars = "id", 
                           measure.vars = c("cost_blind", "cost_maxmin", "cost_minmin", "cost_myopic"),
                           variable.name = "cost") %>%
  dplyr::mutate(algo_name = sapply(stringr::str_split(cost, "_"), function(x) x[[2]])) %>%
  dplyr::select(id, algo_name, value) %>%
  dplyr::rename(cost = value)


algo_shape = algo_mk %>%
  dplyr::inner_join(algo_cost, by=c("id", "algo_name")) %>%
  dplyr::mutate(algo_name = factor(algo_name, 
                                   levels=c("blind", "maxmin", "minmin", "myopic"),
                                   labels = c("Ciego", "MaxMin", "MinMin", "Miope")))
  

algo_summary = algo_shape %>%
  dplyr::group_by(algo_name) %>%
  dplyr::summarise(
    mk_mean = mean(mk),
    mk_sd = sd(mk),
    cost_mean = mean(cost),
    cost_sd = sd(cost)
  ) %>%
  dplyr::rename(
    Algoritmo = algo_name,
    `Makespan` = mk_mean,
    `Desviación estándar M` = mk_sd,
    `Costo` = cost_mean,
    `Desviación estándar C` = cost_sd
  )

algo_data %>%
  dplyr::summarise(
    perc_makespan = sum(blind_makespan_winner) / n(),
    perc_cost = sum(blind_cost_winner) / n(),
    perc_abs_makespan = sum(blind_absolute_makespan_winner) / n(),
    perc_abs_cost = sum(blind_absolute_cost_winner) / n())

xtable_summary = xtable::xtable(algo_summary, 
                                label = 'table:results_makespan', 
                                caption = 'Resultados agregados de los algoritmos')
print(xtable_summary, include.rownames = F)

# Grafica de barras de makespan entre algoritmos
g_makespan = ggplot2::ggplot(algo_summary, 
                aes(x=Algoritmo, y=Makespan, fill=Algoritmo,
                    ymin = Makespan - `Desviación estándar M`, ymax = Makespan + `Desviación estándar M`)) +
  ggplot2::geom_bar(stat = 'identity') +
  ggplot2::geom_errorbar() +
  ggplot2::labs(title = "Makespan promedio por algoritmo") +
  ggplot2::theme(legend.position="none")

# Grafica de barras de costo entre algoritmos
g_cost = ggplot2::ggplot(algo_summary, 
                aes(x=Algoritmo, y=Costo, fill=Algoritmo,
                    ymin = Costo - `Desviación estándar C`, ymax = Costo + `Desviación estándar C`)) +
  ggplot2::geom_bar(stat = 'identity') +
  ggplot2::geom_errorbar() +
  ggplot2::labs(title = "Costo promedio por algoritmo") +
  ggplot2::theme(legend.position="none")

dir.create('./graficas')
ggplot2::ggsave('./graficas/avg_blind-cost_makespan.pdf', plot=g_makespan, width = 4, height = 6, units = 'in')
ggplot2::ggsave('./graficas/avg_blind-cost_cost.pdf', plot=g_cost, width = 4, height = 6, units = 'in')

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_freqpoly(aes( x = cost, colour = algo_name), binwidth=5)

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_freqpoly(aes( x = mk, colour = algo_name), binwidth=0.40)

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_point(aes( x = mk, y = cost, colour = algo_name))

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_bar(aes( x = id, y = mk, fill = algo_name), stat = "identity", position="dodge")

ggplot2::ggplot(algo_shape) +
  ggplot2::geom_bar(aes( x = id, y = cost, fill = algo_name), stat = "identity", position="dodge")

algo_data_absolute = algo_data %>%
   dplyr::filter(mk_blind < mk_myopic & mk_blind < mk_minmin & mk_blind < mk_maxmin)
  
algo_data_some = algo_data %>%
  dplyr::filter(mk_blind <= mk_myopic & mk_blind <= mk_minmin & mk_blind <= mk_maxmin)
