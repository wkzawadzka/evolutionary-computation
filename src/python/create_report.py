import pandas as pd
import sys
from docx import Document
from docx.shared import Inches
from read_solution_files import *
from tables import *
from utils import *
from visualizations import *
from docx.shared import Pt
from datetime import datetime
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT

def save_doc(doc, week_name):
    doc_path = f'../../reports/{week_name}.docx'
    doc.save(doc_path)
    print(f"Summary report saved to '{doc_path}'.")

def create_summary_report(df, week_name):
    doc = Document()

    # authors
    authors_paragraph = doc.add_paragraph()
    authors_run = authors_paragraph.add_run('Authors: Weronika Zawadzka 151943 Eliza Czaplicka 151963') 
    authors_run.font.size = Pt(8) 
    authors_paragraph.alignment = WD_PARAGRAPH_ALIGNMENT.RIGHT

    # date
    date_paragraph = doc.add_paragraph()
    date_run = date_paragraph.add_run(f'Date: {datetime.now().strftime("%B %d, %Y")}')
    date_run.font.size = Pt(8) 
    date_paragraph.alignment = WD_PARAGRAPH_ALIGNMENT.RIGHT

    # note
    paragraph = doc.add_paragraph()
    run = paragraph.add_run('Best solutions have been checked with the solution checker')
    run.font.size = Pt(8) 
    paragraph.alignment = WD_PARAGRAPH_ALIGNMENT.RIGHT

    doc.add_heading(f'Summary Report for {week_name}', level=1)
    p = doc.add_paragraph('Based on methods from ')
    add_hyperlink(p, 'our github project', f"https://github.com/wkzawadzka/evolutionary-computation/tree/master/src/java/src/main/java/evcomp/{week_name}")
    insertHR(p)

    # prepare sections
    doc.add_heading(f'Problem description', level=2)
    add_problem_description(doc)
    doc.add_heading(f'Pseudocode of implemented algorithms', level=2)
    doc.add_page_break()

    # create summary statistics tables
    doc.add_heading(f'Summary performance of each method', level=2)
    p = doc.add_paragraph("")
    insertHR(p)
    for instance in df['instance'].unique():
        doc.add_heading(f'Instance: {instance}', level=3)
        variable_labels, summary_stats = create_summary_table(df, instance)

        for variable, summary_df in summary_stats.items():
            doc.add_heading(f'Summary for {variable_labels[variable]}', level=4)
            add_table(doc, summary_df)
        
        p = doc.add_paragraph("")
        insertHR(p)

    doc.add_page_break()

    # 2D visualisations
    doc.add_heading(f'2D visualisations of best solutions', level=2)
    p = doc.add_paragraph("")
    insertHR(p)
    for instance in df['instance'].unique():
        doc.add_heading(f'Instance: {instance}', level=3)
        generate_visualizations(doc, df, instance)
        p = doc.add_paragraph("")
        insertHR(p)
    doc.add_page_break()

    # printed solutions
    doc.add_heading(f'Best solutions, indices', level=2)
    p = doc.add_paragraph("")
    insertHR(p)
    for instance in df['instance'].unique():
        doc.add_heading(f'Instance: {instance}', level=3)
        print_solutions_with_lowest_fval(doc, df, instance)
    doc.add_page_break()

    # conclusions
    doc.add_heading(f'Conclusions', level=2)

    # save the document
    save_doc(doc, week_name)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python create_report.py <week_name> \n e.g. python create_report.py w1_greedy_heuristics")
        sys.exit(1)

    week_name = sys.argv[1]
    results = read_solution_files(week_name)
    create_summary_report(results, week_name)
